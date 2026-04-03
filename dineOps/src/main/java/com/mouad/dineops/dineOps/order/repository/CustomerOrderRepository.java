package com.mouad.dineops.dineOps.order.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.common.enums.OrderStatus;
import com.mouad.dineops.dineOps.order.entity.CustomerOrder;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

	boolean existsByOrderNumber(String orderNumber);

	Optional<CustomerOrder> findByOrderNumber(String orderNumber);

	List<CustomerOrder> findByBranchIdOrderByCreatedAtDesc(Long branchId);

	List<CustomerOrder> findByBranchIdAndStatusOrderByCreatedAtDesc(Long branchId, OrderStatus status);

	List<CustomerOrder> findByBranchIdAndStatusAndCompletedAtBetween(
			Long branchId,
			OrderStatus status,
			Instant from,
			Instant to);

	List<CustomerOrder> findByCreatedAtBetween(Instant from, Instant to);

	List<CustomerOrder> findByBranchIdAndCreatedAtBetween(Long branchId, Instant from, Instant to);

	List<CustomerOrder> findByStatusAndCompletedAtBetween(OrderStatus status, Instant from, Instant to);
}
