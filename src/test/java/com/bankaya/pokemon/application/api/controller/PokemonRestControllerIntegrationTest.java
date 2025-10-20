package com.bankaya.pokemon.application.api.controller;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for PokemonRestController
 * Tests the REST API endpoints that expose Pokemon data
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Pokemon REST Controller Integration Tests")
class PokemonRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Provider method for successful Pokemon requests
    static Stream<Arguments> provideValidPokemonNames() {
        return Stream.of(
                Arguments.of("pikachu", 25, "pikachu"),
                Arguments.of("bulbasaur", 1, "bulbasaur"),
                Arguments.of("charmander", 4, "charmander"),
                Arguments.of("squirtle", 7, "squirtle")
        );
    }

    @ParameterizedTest(name = "GET /pokemon/{0} should return Pokemon with id {1}")
    @MethodSource("provideValidPokemonNames")
    @DisplayName("Should successfully fetch Pokemon by valid name")
    void testGetPokemonByValidName(String pokemonName, int expectedId, String expectedName) throws Exception {
        // When & Then - Request Pokemon and verify response
        mockMvc.perform(get("/pokemon/{pokemonName}", pokemonName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", equalTo(expectedId)))
                .andExpect(jsonPath("$.name", equalTo(expectedName)))
                .andExpect(jsonPath("$.base_experience", notNullValue()))
                .andExpect(jsonPath("$.abilities", notNullValue()));
    }

    @ParameterizedTest(name = "GET /pokemon/{0} should return 404 or error")
    @CsvSource({
            "nonexistentpokemon123456",
            "invalidpokemonname",
            "pokemon-does-not-exist"
    })
    @DisplayName("Should return error for non-existent Pokemon")
    void testGetPokemonByInvalidName(String pokemonName) throws Exception {
        // When & Then - Request non-existent Pokemon and expect error
        mockMvc.perform(get("/pokemon/{pokemonName}", pokemonName))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle case-insensitive Pokemon names")
    void testCaseInsensitivePokemonNames() throws Exception {
        // When & Then - Request Pokemon with uppercase name
        mockMvc.perform(get("/pokemon/{pokemonName}", "PIKACHU"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", equalTo(25)))
                .andExpect(jsonPath("$.name", equalTo("pikachu")));
    }

    @Test
    @DisplayName("Should return Pokemon with abilities array")
    void testPokemonWithAbilities() throws Exception {
        // When & Then - Request Pikachu and verify abilities
        mockMvc.perform(get("/pokemon/{pokemonName}", "pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abilities", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.abilities[0].name", notNullValue()))
                .andExpect(jsonPath("$.abilities[0].url", notNullValue()));
    }

    @Test
    @DisplayName("Should return Pokemon with held items")
    void testPokemonWithHeldItems() throws Exception {
        // When & Then - Request Pokemon and verify held_items array exists
        mockMvc.perform(get("/pokemon/{pokemonName}", "pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.held_items", notNullValue()));
    }

    @Test
    @DisplayName("Should return Pokemon with location area encounters URL")
    void testPokemonWithLocationAreaEncounters() throws Exception {
        // When & Then - Request Pokemon and verify location_area_encounters exists
        mockMvc.perform(get("/pokemon/{pokemonName}", "pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location_area_encounters", notNullValue()));
    }

    @Test
    @DisplayName("Should handle empty Pokemon name gracefully")
    void testEmptyPokemonName() throws Exception {
        // When & Then - Request with empty name should return 404 or 405
        mockMvc.perform(get("/pokemon/"))
                .andExpect(status().is4xxClientError());
    }
}
