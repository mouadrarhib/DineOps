package com.mouad.dineops.dineOps.order.repository;

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
}
