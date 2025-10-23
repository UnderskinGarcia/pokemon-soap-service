package com.bankaya.pokemon.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTOs for SOAP Proxy REST endpoints
 * These are simplified REST representations of SOAP responses
 */
public class SoapProxyResponses {

    private SoapProxyResponses() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Pokemon Ability information")
    public static class AbilityDTO {
        @Schema(description = "Name of the ability", example = "static")
        private String name;

        @Schema(description = "URL to ability details", example = "https://pokeapi.co/api/v2/ability/9/")
        private String url;

        @Schema(description = "Whether this is a hidden ability", example = "false")
        private Boolean isHidden;

        @Schema(description = "Ability slot number", example = "1")
        private Integer slot;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Held Item information")
    public static class HeldItemDTO {
        @Schema(description = "Name of the item", example = "light-ball")
        private String name;

        @Schema(description = "URL to item details", example = "https://pokeapi.co/api/v2/item/213/")
        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "List of Pokemon abilities")
    public static class AbilitiesResponse {
        @Schema(description = "List of abilities")
        private List<AbilityDTO> abilities;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Pokemon base experience")
    public static class BaseExperienceResponse {
        @Schema(description = "Base experience points", example = "112")
        private Integer baseExperience;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "List of held items")
    public static class HeldItemsResponse {
        @Schema(description = "List of items the Pokemon can hold")
        private List<HeldItemDTO> heldItems;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Pokemon ID")
    public static class IdResponse {
        @Schema(description = "Pokemon numeric ID", example = "25")
        private Long id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Pokemon name")
    public static class NameResponse {
        @Schema(description = "Pokemon name", example = "pikachu")
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Location area encounters URL")
    public static class LocationEncountersResponse {
        @Schema(description = "URL to location encounters", example = "https://pokeapi.co/api/v2/pokemon/25/encounters")
        private String locationAreaEncounters;
    }
}
