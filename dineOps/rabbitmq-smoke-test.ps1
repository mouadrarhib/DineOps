param(
    [switch]$SkipComposeUp,
    [switch]$NoBuild
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Invoke-DockerCommand {
    param(
        [Parameter(Mandatory = $true)][string[]]$Args,
        [switch]$AllowFailure
    )

    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $output = & docker @Args 2>&1
    $ErrorActionPreference = $previousErrorActionPreference
    if (-not $AllowFailure -and $LASTEXITCODE -ne 0) {
        throw "Command failed: docker $($Args -join ' ')`n$output"
    }
    return $output
}

function Wait-Until {
    param(
        [Parameter(Mandatory = $true)][scriptblock]$Condition,
        [int]$TimeoutSeconds = 90,
        [int]$IntervalSeconds = 2,
        [Parameter(Mandatory = $true)][string]$FailureMessage
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (& $Condition) {
            return
        }
        Start-Sleep -Seconds $IntervalSeconds
    }

    throw $FailureMessage
}

function Get-BasicAuthHeader {
    param([string]$Username, [string]$Password)

    $raw = "${Username}:${Password}"
    $encoded = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($raw))
    return "Basic $encoded"
}

function Get-QueueState {
    param(
        [string]$QueueName,
        [string]$AuthHeader
    )

    $urlQueueName = [uri]::EscapeDataString($QueueName)
    $uri = "http://localhost:15672/api/queues/%2F/$urlQueueName"
    return Invoke-RestMethod -Method Get -Uri $uri -Headers @{ Authorization = $AuthHeader }
}

function Publish-Event {
    param(
        [string]$RoutingKey,
        [hashtable]$Payload,
        [string]$AuthHeader
    )

    $payloadString = $Payload | ConvertTo-Json -Compress
    $bodyObject = @{
        properties       = @{}
        routing_key      = $RoutingKey
        payload          = $payloadString
        payload_encoding = "string"
    }

    $uri = "http://localhost:15672/api/exchanges/%2F/dineops.events.exchange/publish"
    $response = Invoke-RestMethod -Method Post -Uri $uri -Headers @{ Authorization = $AuthHeader } -ContentType "application/json" -Body ($bodyObject | ConvertTo-Json -Compress)
    if (-not $response.routed) {
        throw "Publish failed (not routed) for routing key: $RoutingKey"
    }
}

Write-Host "=== RabbitMQ smoke test starting ==="

if (-not $SkipComposeUp) {
    Write-Host "Bringing up Docker stack..."
    if ($NoBuild) {
        Invoke-DockerCommand -Args @("compose", "up", "-d") | Out-Null
    } else {
        Invoke-DockerCommand -Args @("compose", "up", "-d", "--build") | Out-Null
    }
}

Write-Host "Waiting for app health endpoint..."
Wait-Until -Condition {
    try {
        $health = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/health"
        return $health.data.status -eq "UP"
    } catch {
        return $false
    }
} -FailureMessage "App did not become healthy in time."

$authHeader = Get-BasicAuthHeader -Username "dineops" -Password "dineops123"

Write-Host "Validating RabbitMQ topology..."
$exchange = Invoke-RestMethod -Method Get -Uri "http://localhost:15672/api/exchanges/%2F/dineops.events.exchange" -Headers @{ Authorization = $authHeader }
if ($exchange.type -ne "topic") {
    throw "Unexpected exchange type: $($exchange.type)"
}

$queues = @(
    "dineops.events.order-confirmed.queue",
    "dineops.events.reservation-approved.queue",
    "dineops.events.low-stock-detected.queue"
)

foreach ($q in $queues) {
    $null = Get-QueueState -QueueName $q -AuthHeader $authHeader
}

Write-Host "Reading baseline notification count..."
$baselineRaw = (Invoke-DockerCommand -Args @("exec", "dineops-postgres", "psql", "-U", "postgres", "-d", "dineops", "-t", "-A", "-c", "select coalesce(max(id),0), count(*) from notifications;")) -join ""
$baselineParts = $baselineRaw.Trim().Split("|")
$baselineMaxId = [int64]$baselineParts[0]
$baselineCount = [int64]$baselineParts[1]

$testSuffix = Get-Date -Format "yyyyMMddHHmmss"

Write-Host "Publishing events..."
Publish-Event -RoutingKey "dineops.event.order.confirmed" -Payload @{
    orderId      = 900001
    orderNumber  = "ORD-SMOKE-$testSuffix"
    branchId     = 1
    totalAmount  = 49.90
    confirmedAt  = (Get-Date).ToUniversalTime().ToString("o")
} -AuthHeader $authHeader

Publish-Event -RoutingKey "dineops.event.reservation.approved" -Payload @{
    reservationId   = 900002
    customerName    = "Smoke Test"
    customerEmail   = "smoke.$testSuffix@example.com"
    reservationTime = (Get-Date).ToUniversalTime().AddHours(2).ToString("o")
} -AuthHeader $authHeader

Publish-Event -RoutingKey "dineops.event.low-stock.detected" -Payload @{
    branchId           = 1
    inventoryItemId    = 900003
    ingredientName     = "Smoke Ingredient"
    quantityAvailable  = 0.500
    unit               = "kg"
} -AuthHeader $authHeader

Write-Host "Waiting for queues to drain..."
Wait-Until -Condition {
    foreach ($q in $queues) {
        $state = Get-QueueState -QueueName $q -AuthHeader $authHeader
        if (($state.messages -as [int]) -gt 0) {
            return $false
        }
    }
    return $true
} -FailureMessage "Queues did not drain to zero messages in time."

Write-Host "Validating notification rows were created by consumers..."
Wait-Until -Condition {
    $countRaw = (Invoke-DockerCommand -Args @("exec", "dineops-postgres", "psql", "-U", "postgres", "-d", "dineops", "-t", "-A", "-c", "select count(*) from notifications where id > $baselineMaxId;")) -join ""
    $delta = [int64]$countRaw.Trim()
    return $delta -ge 3
} -FailureMessage "Expected at least 3 notifications from events, but they were not created in time."

$summaryRows = Invoke-DockerCommand -Args @(
    "exec", "dineops-postgres", "psql", "-U", "postgres", "-d", "dineops", "-t", "-A", "-c",
    "select related_entity_type, count(*) from notifications where id > $baselineMaxId group by related_entity_type order by related_entity_type;"
)

$finalRaw = (Invoke-DockerCommand -Args @("exec", "dineops-postgres", "psql", "-U", "postgres", "-d", "dineops", "-t", "-A", "-c", "select count(*) from notifications;")) -join ""
$finalCount = [int64]$finalRaw.Trim()

Write-Host "=== RabbitMQ smoke test PASSED ==="
Write-Host "Baseline notifications: $baselineCount"
Write-Host "Final notifications:    $finalCount"
Write-Host "Added notifications:    $($finalCount - $baselineCount)"
Write-Host "New rows by related_entity_type:"
$summaryRows | ForEach-Object { Write-Host " - $($_.Trim())" }
