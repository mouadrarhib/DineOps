package com.mouad.dineops.dineOps.menu.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.menu.entity.MenuItemIngredient;

public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, Long> {

	boolean existsByMenuItemIdAndIngredientId(Long menuItemId, Long ingredientId);

	List<MenuItemIngredient> findByMenuItemIdIn(Collection<Long> menuItemIds);
}
