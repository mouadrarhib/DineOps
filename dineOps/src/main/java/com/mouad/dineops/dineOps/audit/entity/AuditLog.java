package com.mouad.dineops.dineOps.audit.entity;

import com.mouad.dineops.dineOps.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

	@Column(name = "actor_user_id")
	private Long actorUserId;

	@Column(name = "actor_email", length = 150)
	private String actorEmail;

	@Column(nullable = false, length = 100)
	private String action;

	@Column(name = "entity_type", nullable = false, length = 100)
	private String entityType;

	@Column(name = "entity_id")
	private Long entityId;

	@Column(name = "branch_id")
	private Long branchId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String details;
}
