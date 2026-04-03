package com.mouad.dineops.dineOps.audit.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.audit.entity.AuditLog;
import com.mouad.dineops.dineOps.audit.repository.AuditLogRepository;
import com.mouad.dineops.dineOps.auth.security.AppUserPrincipal;

@Service
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	public AuditLogService(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	@Transactional
	public void log(String action, String entityType, Long entityId, Long branchId, String details) {
		AuditLog auditLog = new AuditLog();
		auditLog.setAction(action);
		auditLog.setEntityType(entityType);
		auditLog.setEntityId(entityId);
		auditLog.setBranchId(branchId);
		auditLog.setDetails(details == null ? "-" : details);

		AppUserPrincipal principal = getCurrentUserPrincipal();
		if (principal != null) {
			auditLog.setActorUserId(principal.getId());
			auditLog.setActorEmail(principal.getUsername());
		}

		auditLogRepository.save(auditLog);
	}

	private AppUserPrincipal getCurrentUserPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
			return null;
		}
		return principal;
	}
}
