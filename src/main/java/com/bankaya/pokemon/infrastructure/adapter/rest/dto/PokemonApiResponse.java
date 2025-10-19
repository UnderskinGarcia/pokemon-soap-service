package com.bankaya.pokemon.infrastructure.adapter.rest.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for PokeAPI Response
 * Maps the JSON response from <a href="https://pokeapi.co/api/v2/pokemon/">...</a>{name}
 */

public record PokemonApiResponse(

        Long id,
        String name,

        @JsonProperty("base_experience")
        Integer baseExperience,

        List<AbilitySlot> abilities,

        @JsonProperty("held_items")
        List<HeldItemSlot> heldItems,

        @JsonProperty("location_area_encounters")
        String locationAreaEncounters
) {
    public record AbilitySlot(
            Ability ability,

            @JsonProperty("is_hidden")
            Boolean isHidden,

            Integer slot
    ) {
    }

    public record Ability(
            String name,
            String url
    ) {
    }

    public record HeldItemSlot(
            HeldItem item
    ) {
    }

    public record HeldItem(
            String name,
            String url
    ) {
    }
}
