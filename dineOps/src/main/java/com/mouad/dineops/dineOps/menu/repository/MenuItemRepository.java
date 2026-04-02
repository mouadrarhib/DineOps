package com.mouad.dineops.dineOps.menu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mouad.dineops.dineOps.menu.entity.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

	@Query("""
			select mi
			from MenuItem mi
			where mi.category.branch.id = :branchId
			  and (:active is null or mi.active = :active)
			  and (:available is null or mi.available = :available)
			  and (:search is null or trim(:search) = '' or lower(mi.name) like lower(concat('%', :search, '%')))
			""")
	Page<MenuItem> findPageByBranchAndFilters(
			@Param("branchId") Long branchId,
			@Param("active") Boolean active,
			@Param("available") Boolean available,
			@Param("search") String search,
			Pageable pageable);
}
