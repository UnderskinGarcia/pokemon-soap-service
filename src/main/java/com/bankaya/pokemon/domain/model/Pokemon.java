package com.bankaya.pokemon.domain.model;

import java.util.List;

import lombok.Builder;
import lombok.With;

/**
 * Pokémon Domain Entity - Core business model
 * This model represents the Pokémon in the domain layer (Hexagonal Architecture)
 */
@Builder
@With
public record Pokemon(
        Long id,
        String name,
        Integer baseExperience,
        List<Ability> abilities,
        List<HeldItem> heldItems,
        String locationAreaEncounters
) {
    @Builder
    public record Ability(
            String name,
            String url,
            Boolean isHidden,
            Integer slot
    ) {
    }

    @Builder
    public record HeldItem(
            String name,
            String url) {
    }
}
