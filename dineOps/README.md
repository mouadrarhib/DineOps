# DineOps Backend

## Environment variables (Windows)

This project reads database credentials from environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

You can copy `.env.example` as reference values.

### PowerShell (current terminal session)

```powershell
$env:DB_URL="jdbc:postgresql://aws-1-eu-west-2.pooler.supabase.com:5432/postgres?sslmode=require"
$env:DB_USERNAME="your_db_username"
$env:DB_PASSWORD="your_db_password"
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.10"
mvn spring-boot:run
```

### CMD (current terminal session)

```bat
set DB_URL=jdbc:postgresql://aws-1-eu-west-2.pooler.supabase.com:5432/postgres?sslmode=require
set DB_USERNAME=your_db_username
set DB_PASSWORD=your_db_password
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10
mvn spring-boot:run
```

### Persist variables for your user (PowerShell)

```powershell
[Environment]::SetEnvironmentVariable("DB_URL", "jdbc:postgresql://aws-1-eu-west-2.pooler.supabase.com:5432/postgres?sslmode=require", "User")
[Environment]::SetEnvironmentVariable("DB_USERNAME", "your_db_username", "User")
[Environment]::SetEnvironmentVariable("DB_PASSWORD", "your_db_password", "User")
```

After persisting them, restart your terminal before running Maven.
