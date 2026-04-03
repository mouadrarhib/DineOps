package com.mouad.dineops.dineOps.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.audit.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
