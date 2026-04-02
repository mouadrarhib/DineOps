package com.mouad.dineops.dineOps.inventory.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.common.exception.ConflictException;
import com.mouad.dineops.dineOps.inventory.dto.CreateIngredientRequest;
import com.mouad.dineops.dineOps.inventory.dto.IngredientResponse;
import com.mouad.dineops.dineOps.inventory.entity.Ingredient;
import com.mouad.dineops.dineOps.inventory.repository.IngredientRepository;

@Service
public class IngredientService {

	private final IngredientRepository ingredientRepository;

	public IngredientService(IngredientRepository ingredientRepository) {
		this.ingredientRepository = ingredientRepository;
	}

	@Transactional
	public IngredientResponse createIngredient(CreateIngredientRequest request) {
		String normalizedName = request.name().trim();
		if (ingredientRepository.existsByNameIgnoreCase(normalizedName)) {
			throw new ConflictException("Ingredient already exists: " + normalizedName);
		}

		Ingredient ingredient = new Ingredient();
		ingredient.setName(normalizedName);
		ingredient.setUnit(request.unit().trim().toUpperCase());
		ingredient.setMinThreshold(request.minThreshold());
		ingredient.setActive(request.active() == null ? true : request.active());

		return toResponse(ingredientRepository.save(ingredient));
	}

	@Transactional(readOnly = true)
	public List<IngredientResponse> listIngredients(Boolean activeOnly) {
		List<Ingredient> ingredients = Boolean.TRUE.equals(activeOnly)
				? ingredientRepository.findByActiveTrueOrderByNameAsc()
				: ingredientRepository.findAll().stream().sorted(java.util.Comparator.comparing(Ingredient::getName)).toList();

		return ingredients.stream().map(this::toResponse).toList();
	}

	private IngredientResponse toResponse(Ingredient ingredient) {
		return new IngredientResponse(
				ingredient.getId(),
				ingredient.getName(),
				ingredient.getUnit(),
				ingredient.getMinThreshold(),
				ingredient.isActive(),
				ingredient.getCreatedAt(),
				ingredient.getUpdatedAt());
	}
}
