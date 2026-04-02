package com.mouad.dineops.dineOps.menu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mouad.dineops.dineOps.menu.entity.MenuCategory;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

	@Query("""
			select mc
			from MenuCategory mc
			where mc.branch.id = :branchId
			  and (:active is null or mc.active = :active)
			  and (:search is null or trim(:search) = '' or lower(mc.name) like lower(concat('%', :search, '%')))
			""")
	Page<MenuCategory> findPageByBranchAndFilters(
			@Param("branchId") Long branchId,
			@Param("active") Boolean active,
			@Param("search") String search,
			Pageable pageable);
}
