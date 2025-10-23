package com.bankaya.pokemon.application.rest.controller;

import com.bankaya.pokemon.domain.exception.BadRequestException;
import com.bankaya.pokemon.domain.exception.PokemonNotFoundException;
import com.bankaya.pokemon.domain.model.Pokemon;
import com.bankaya.pokemon.domain.ports.PokemonApiPort;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit/Integration tests for PokemonRestController
 * Uses MockMvc and Mockito to test REST endpoints without external dependencies
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Pokemon REST Controller Tests with MockMvc")
class PokemonRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PokemonApiPort pokemonApiPort;

    // Provider method for successful Pokemon requests
    static Stream<Arguments> provideValidPokemonNames() {
        return Stream.of(
                Arguments.of("pikachu", 25L, "pikachu", 112),
                Arguments.of("bulbasaur", 1L, "bulbasaur", 64),
                Arguments.of("charmander", 4L, "charmander", 62),
                Arguments.of("squirtle", 7L, "squirtle", 63)
        );
    }

    @ParameterizedTest(name = "GET /pokemon/{0} should return Pokemon with id {1}")
    @MethodSource("provideValidPokemonNames")
    @DisplayName("Should successfully fetch Pokemon by valid name")
    void testGetPokemonByValidName(String pokemonName, Long expectedId, String expectedName, Integer baseExp) throws Exception {
        // Given
        Pokemon mockPokemon = new Pokemon(
                expectedId,
                expectedName,
                baseExp,
                List.of(new Pokemon.Ability("static", "https://pokeapi.co/api/v2/ability/9/", false, 1)),
                List.of(),
                "https://pokeapi.co/api/v2/pokemon/" + expectedId + "/encounters"
        );

        when(pokemonApiPort.fetchPokemonByName(pokemonName)).thenReturn(mockPokemon);

        // When & Then
        mockMvc.perform(get("/pokemon/{pokemonName}", pokemonName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(expectedId.intValue())))
                .andExpect(jsonPath("$.name", is(expectedName)))
                .andExpect(jsonPath("$.base_experience", is(baseExp)))
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
        // Given
        when(pokemonApiPort.fetchPokemonByName(pokemonName))
                .thenThrow(new PokemonNotFoundException(pokemonName));

        // When & Then
        mockMvc.perform(get("/pokemon/{pokemonName}", pokemonName))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle case-insensitive Pokemon names")
    void testCaseInsensitivePokemonNames() throws Exception {
        // Given
        Pokemon mockPokemon = new Pokemon(
                25L,
                "pikachu",
                112,
                List.of(new Pokemon.Ability("static", "https://pokeapi.co/api/v2/ability/9/", false, 1)),
                List.of(),
                "https://pokeapi.co/api/v2/pokemon/25/encounters"
        );

        when(pokemonApiPort.fetchPokemonByName(anyString())).thenReturn(mockPokemon);

        // When & Then
        mockMvc.perform(get("/pokemon/{pokemonName}", "PIKACHU"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(25)))
                .andExpect(jsonPath("$.name", is("pikachu")));
    }

    @Test
    @DisplayName("Should return Pokemon with abilities array")
    void testPokemonWithAbilities() throws Exception {
        // Given
        Pokemon mockPokemon = new Pokemon(
                25L,
                "pikachu",
                112,
                List.of(
                        new Pokemon.Ability("static", "https://pokeapi.co/api/v2/ability/9/", false, 1),
                        new Pokemon.Ability("lightning-rod", "https://pokeapi.co/api/v2/ability/31/", true, 3)
                ),
                List.of(),
                "https://pokeapi.co/api/v2/pokemon/25/encounters"
        );

        when(pokemonApiPort.fetchPokemonByName("pikachu")).thenReturn(mockPokemon);

        // When & Then
        mockMvc.perform(get("/pokemon/{pokemonName}", "pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abilities", hasSize(2)))
                .andExpect(jsonPath("$.abilities[0].name", is("static")))
                .andExpect(jsonPath("$.abilities[0].url", notNullValue()))
                .andExpect(jsonPath("$.abilities[1].name", is("lightning-rod")));
    }

    @Test
    @DisplayName("Should return Pokemon with held items")
    void testPokemonWithHeldItems() throws Exception {
        // Given
        Pokemon mockPokemon = new Pokemon(
                25L,
                "pikachu",
                112,
                List.of(),
                List.of(new Pokemon.HeldItem("light-ball", "https://pokeapi.co/api/v2/item/213/")),
                "https://pokeapi.co/api/v2/pokemon/25/encounters"
        );

        when(pokemonApiPort.fetchPokemonByName("pikachu")).thenReturn(mockPokemon);

        // When & Then
        mockMvc.perform(get("/pokemon/{pokemonName}", "pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.held_items", notNullValue()))
                .andExpect(jsonPath("$.held_items", hasSize(1)))
                .andExpect(jsonPath("$.held_items[0].name", is("light-ball")));
    }

    @Test
    @DisplayName("Should return Pokemon with location area encounters URL")
    void testPokemonWithLocationAreaEncounters() throws Exception {
        // Given
        Pokemon mockPokemon = new Pokemon(
                25L,
                "pikachu",
                112,
                List.of(),
                List.of(),
                "https://pokeapi.co/api/v2/pokemon/25/encounters"
        );

        when(pokemonApiPort.fetchPokemonByName("pikachu")).thenReturn(mockPokemon);

        // When & Then
        mockMvc.perform(get("/pokemon/{pokemonName}", "pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location_area_encounters", notNullValue()))
                .andExpect(jsonPath("$.location_area_encounters", containsString("encounters")));
    }

    @Test
    @DisplayName("Should handle empty Pokemon name gracefully")
    void testEmptyPokemonName() throws Exception {
        // When & Then - Request with empty name should return 404
        mockMvc.perform(get("/pokemon/"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle BadRequestException")
    void testBadRequestException() throws Exception {
        // Given
        when(pokemonApiPort.fetchPokemonByName(anyString()))
                .thenThrow(new BadRequestException("Pokemon name cannot be null or empty"));

        // When & Then
        mockMvc.perform(get("/pokemon/{pokemonName}", "invalid"))
                .andExpect(status().is4xxClientError());
    }
}
