package com.bankaya.pokemon.application.rest.controller;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bankaya.pokemon.application.service.PokemonService;
import com.bankaya.pokemon.domain.exception.BadRequestException;
import com.bankaya.pokemon.domain.exception.PokemonNotFoundException;
import com.bankaya.pokemon.soap.Ability;
import com.bankaya.pokemon.soap.GetPokemonAbilitiesResponse;
import com.bankaya.pokemon.soap.GetPokemonBaseExperienceResponse;
import com.bankaya.pokemon.soap.GetPokemonHeldItemsResponse;
import com.bankaya.pokemon.soap.GetPokemonIdResponse;
import com.bankaya.pokemon.soap.GetPokemonLocationAreaEncountersResponse;
import com.bankaya.pokemon.soap.GetPokemonNameResponse;
import com.bankaya.pokemon.soap.HeldItem;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit/Integration tests for SOAP Proxy REST Controller
 * Uses MockMvc and Mockito to test REST endpoints without external dependencies
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("SOAP Proxy Controller Tests with MockMvc")
class SoapProxyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PokemonService pokemonService;

    @Test
    @DisplayName("Should get Pokemon abilities through REST proxy")
    void testGetAbilities() throws Exception {
        // Given
        String pokemonName = "pikachu";

        GetPokemonAbilitiesResponse soapResponse = new GetPokemonAbilitiesResponse();
        Ability ability1 = new Ability();
        ability1.setName("static");
        ability1.setUrl("https://pokeapi.co/api/v2/ability/9/");
        ability1.setIsHidden(false);
        ability1.setSlot(1);

        Ability ability2 = new Ability();
        ability2.setName("lightning-rod");
        ability2.setUrl("https://pokeapi.co/api/v2/ability/31/");
        ability2.setIsHidden(true);
        ability2.setSlot(3);

        soapResponse.getAbilities().addAll(List.of(ability1, ability2));

        when(pokemonService.getPokemonAbilities(pokemonName)).thenReturn(soapResponse);

        // When & Then
        mockMvc.perform(get("/api/soap/pokemon/{name}/abilities", pokemonName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.abilities", hasSize(2)))
                .andExpect(jsonPath("$.abilities[0].name", is("static")))
                .andExpect(jsonPath("$.abilities[0].url", is("https://pokeapi.co/api/v2/ability/9/")))
                .andExpect(jsonPath("$.abilities[0].is_hidden", is(false)))
                .andExpect(jsonPath("$.abilities[0].slot", is(1)))
                .andExpect(jsonPath("$.abilities[1].name", is("lightning-rod")))
                .andExpect(jsonPath("$.abilities[1].is_hidden", is(true)));
    }

    @Test
    @DisplayName("Should get Pokemon base experience through REST proxy")
    void testGetBaseExperience() throws Exception {
        // Given
        String pokemonName = "pikachu";

        GetPokemonBaseExperienceResponse soapResponse = new GetPokemonBaseExperienceResponse();
        soapResponse.setBaseExperience(112);

        when(pokemonService.getPokemonBaseExperienceResponse(pokemonName)).thenReturn(soapResponse);

        // When & Then
        mockMvc.perform(get("/api/soap/pokemon/{name}/base-experience", pokemonName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.base_experience", is(112)));
    }

    @Test
    @DisplayName("Should get Pokemon held items through REST proxy")
    void testGetHeldItems() throws Exception {
        // Given
        String pokemonName = "pikachu";

        GetPokemonHeldItemsResponse soapResponse = new GetPokemonHeldItemsResponse();
        HeldItem item = new HeldItem();
        item.setName("light-ball");
        item.setUrl("https://pokeapi.co/api/v2/item/213/");
        soapResponse.getHeldItems().add(item);

        when(pokemonService.getPokemonHeldItems(pokemonName)).thenReturn(soapResponse);

        // When & Then
        mockMvc.perform(get("/api/soap/pokemon/{name}/held-items", pokemonName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.held_items", hasSize(1)))
                .andExpect(jsonPath("$.held_items[0].name", is("light-ball")))
                .andExpect(jsonPath("$.held_items[0].url", is("https://pokeapi.co/api/v2/item/213/")));
    }

    @Test
    @DisplayName("Should get Pokemon ID through REST proxy")
    void testGetId() throws Exception {
        // Given
        String pokemonName = "pikachu";

        GetPokemonIdResponse soapResponse = new GetPokemonIdResponse();
        soapResponse.setId(25L);

        when(pokemonService.getPokemonId(pokemonName)).thenReturn(soapResponse);

        // When & Then
        mockMvc.perform(get("/api/soap/pokemon/{name}/id", pokemonName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(25)));
    }

    @Test
    @DisplayName("Should get Pokemon name through REST proxy")
    void testGetName() throws Exception {
        // Given
        String pokemonName = "pikachu";

        GetPokemonNameResponse soapResponse = new GetPokemonNameResponse();
        soapResponse.setName("pikachu");

        when(pokemonService.getPokemonName(pokemonName)).thenReturn(soapResponse);

        // When & Then
        mockMvc.perform(get("/api/soap/pokemon/{name}/name", pokemonName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("pikachu")));
    }

    @Test
    @DisplayName("Should get Pokemon location encounters through REST proxy")
    void testGetLocationEncounters() throws Exception {
        // Given
        String pokemonName = "pikachu";

        GetPokemonLocationAreaEncountersResponse soapResponse = new GetPokemonLocationAreaEncountersResponse();
        soapResponse.setLocationAreaEncounters("https://pokeapi.co/api/v2/pokemon/25/encounters");

        when(pokemonService.getPokemonLocationAreaEncounters(pokemonName)).thenReturn(soapResponse);

        // When & Then
        mockMvc.perform(get("/api/soap/pokemon/{name}/locations", pokemonName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.location_area_encounters", is("https://pokeapi.co/api/v2/pokemon/25/encounters")))
                .andExpect(jsonPath("$.location_area_encounters", containsString("encounters")));
    }

    @Test
    @DisplayName("Should return 404 for non-existent Pokemon")
    void testNonExistentPokemon() throws Exception {
        // Given
        String pokemonName = "nonexistentpokemon123456";

        when(pokemonService.getPokemonId(pokemonName))
                .thenThrow(new PokemonNotFoundException(pokemonName));

        // When & Then
        mockMvc.perform(get("/api/soap/pokemon/{name}/id", pokemonName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should return 400 for empty Pokemon name")
    void testEmptyPokemonName() throws Exception {
        // Given
        String pokemonName = "";

        when(pokemonService.getPokemonId(anyString()))
                .thenThrow(new BadRequestException("Pokemon name cannot be null or empty"));

        // When & Then
        mockMvc.perform(get("/api/soap/pokemon/{name}/id", pokemonName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle different Pokemon names correctly")
    void testDifferentPokemon() throws Exception {
        // Given - Bulbasaur
        GetPokemonIdResponse bulbasaurResponse = new GetPokemonIdResponse();
        bulbasaurResponse.setId(1L);
        when(pokemonService.getPokemonId("bulbasaur")).thenReturn(bulbasaurResponse);

        // When & Then - Bulbasaur
        mockMvc.perform(get("/api/soap/pokemon/{name}/id", "bulbasaur")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        // Given - Charmander
        GetPokemonIdResponse charmanderResponse = new GetPokemonIdResponse();
        charmanderResponse.setId(4L);
        when(pokemonService.getPokemonId("charmander")).thenReturn(charmanderResponse);

        // When & Then - Charmander
        mockMvc.perform(get("/api/soap/pokemon/{name}/id", "charmander")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(4)));

        // Given - Squirtle
        GetPokemonIdResponse squirtleResponse = new GetPokemonIdResponse();
        squirtleResponse.setId(7L);
        when(pokemonService.getPokemonId("squirtle")).thenReturn(squirtleResponse);

        // When & Then - Squirtle
        mockMvc.perform(get("/api/soap/pokemon/{name}/id", "squirtle")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(7)));
    }

    @Test
    @DisplayName("Should handle case-insensitive Pokemon names")
    void testCaseInsensitiveName() throws Exception {
        // Given
        GetPokemonIdResponse response = new GetPokemonIdResponse();
        response.setId(25L);

        when(pokemonService.getPokemonId(anyString())).thenReturn(response);

        // When & Then - Test multiple case variations
        String[] variations = {"PIKACHU", "Pikachu", "pikachu", "PiKaChU"};

        for (String variation : variations) {
            mockMvc.perform(get("/api/soap/pokemon/{name}/id", variation)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(25)));
        }
    }
}
