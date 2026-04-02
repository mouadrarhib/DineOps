package com.mouad.dineops.dineOps.menu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.menu.entity.MenuCategory;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

	List<MenuCategory> findByBranchIdOrderByDisplayOrderAscIdAsc(Long branchId);
}
