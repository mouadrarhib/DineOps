package com.mouad.dineops.dineOps.menu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.menu.entity.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

	List<MenuItem> findByCategoryBranchIdOrderByNameAsc(Long branchId);
}
