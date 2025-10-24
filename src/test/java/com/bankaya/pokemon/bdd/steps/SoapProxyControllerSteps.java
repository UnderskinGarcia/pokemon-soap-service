package com.bankaya.pokemon.bdd.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.bankaya.pokemon.application.service.PokemonService;
import com.bankaya.pokemon.domain.exception.PokemonNotFoundException;
import com.bankaya.pokemon.soap.Ability;
import com.bankaya.pokemon.soap.GetPokemonAbilitiesResponse;
import com.bankaya.pokemon.soap.GetPokemonBaseExperienceResponse;
import com.bankaya.pokemon.soap.GetPokemonHeldItemsResponse;
import com.bankaya.pokemon.soap.GetPokemonIdResponse;
import com.bankaya.pokemon.soap.GetPokemonLocationAreaEncountersResponse;
import com.bankaya.pokemon.soap.GetPokemonNameResponse;
import com.bankaya.pokemon.soap.HeldItem;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Step definitions for SOAP Proxy Controller BDD tests
 */
public class SoapProxyControllerSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PokemonService pokemonService;

    private int lastStatusCode;

    @Given("the SOAP service is mocked")
    public void soapServiceIsMocked() {
        // Service is already mocked via @MockitoBean
    }

    @Given("I have mocked Pokemon {string} with {int} abilities")
    public void mockPokemonWithAbilities(String pokemonName, int abilityCount) {
        GetPokemonAbilitiesResponse response = new GetPokemonAbilitiesResponse();

        for (int i = 0; i < abilityCount; i++) {
            Ability ability = new Ability();
            ability.setName(i == 0 ? "static" : "lightning-rod");
            ability.setUrl(i == 0 ? "https://pokeapi.co/api/v2/ability/9/" : "https://pokeapi.co/api/v2/ability/31/");
            ability.setIsHidden(i != 0);
            ability.setSlot(i + 1);
            response.getAbilities().add(ability);
        }

        // Use case-insensitive matching for pokemon name
        when(pokemonService.getPokemonAbilities(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(response);
    }

    @Given("I have mocked Pokemon {string} with base experience {int}")
    public void mockPokemonWithBaseExperience(String pokemonName, int baseExp) {
        GetPokemonBaseExperienceResponse response = new GetPokemonBaseExperienceResponse();
        response.setBaseExperience(baseExp);
        // Use case-insensitive matching for pokemon name
        when(pokemonService.getPokemonBaseExperienceResponse(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(response);
    }

    @Given("I have mocked Pokemon {string} with held item {string}")
    public void mockPokemonWithHeldItem(String pokemonName, String itemName) {
        GetPokemonHeldItemsResponse response = new GetPokemonHeldItemsResponse();
        HeldItem item = new HeldItem();
        item.setName(itemName);
        item.setUrl("https://pokeapi.co/api/v2/item/213/");
        response.getHeldItems().add(item);

        // Use case-insensitive matching for pokemon name
        when(pokemonService.getPokemonHeldItems(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(response);
    }

    @Given("I have mocked Pokemon {string} with ID {int}")
    public void mockPokemonWithId(String pokemonName, long id) {
        GetPokemonIdResponse response = new GetPokemonIdResponse();
        response.setId(id);

        // Register mock with case-insensitive matching using argThat
        when(pokemonService.getPokemonId(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(response);
    }

    @Given("I have mocked Pokemon {string} with name {string}")
    public void mockPokemonWithName(String pokemonName, String name) {
        GetPokemonNameResponse response = new GetPokemonNameResponse();
        response.setName(name);
        // Use case-insensitive matching for pokemon name
        when(pokemonService.getPokemonName(
                argThat(arg -> arg != null && arg.equalsIgnoreCase(pokemonName))
        )).thenReturn(response);
    }

    @Given("I have mocked Pokemon {string} with location {string}")
    public void mockPokemonWithLocation(String pokemonName, String location) {
        GetPokemonLocationAreaEncountersResponse response = new GetPokemonLocationAreaEncountersResponse();
        response.setLocationAreaEncounters(location);
        // Use case-insensitive matching for pokemon name
        when(pokemonService.getPokemonLocationAreaEncounters(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(response);
    }

    @Given("the Pokemon {string} does not exist")
    public void pokemonDoesNotExist(String pokemonName) {
        // Use case-insensitive matching for pokemon name
        when(pokemonService.getPokemonId(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenThrow(new PokemonNotFoundException(pokemonName));
    }

    @Given("I have an empty Pokemon name")
    public void emptyPokemonName() {
        // Empty name will be handled by the endpoint
    }

    @And("the response should contain base_experience value {int}")
    public void responseContainsBaseExperience(int baseExp) throws Exception {
        mockMvc.perform(get("/api/soap/pokemon/pikachu/base-experience"))
                .andExpect(jsonPath("$.base_experience", is(baseExp)));
    }

    @And("the response should contain {int} abilities")
    public void responseContainsAbilities(int number) throws Exception {
        mockMvc.perform(get("/api/soap/pokemon/pikachu/abilities"))
                .andExpect(jsonPath("$.abilities.length()", is(number)));
    }

    @And("the response should contain {int} held item")
    public void responseContainsHeldItems(int number) throws Exception {
        mockMvc.perform(get("/api/soap/pokemon/pikachu/held-items"))
                .andExpect(jsonPath("$.held_items.length()", is(number)));
    }

    @And("the first ability should be {string}")
    public void firstAbilityShouldBe(String ability) throws Exception {
        mockMvc.perform(get("/api/soap/pokemon/pikachu/abilities"))
                .andExpect(jsonPath("$.abilities[0].name", is(ability)));
    }

    @And("the first held item should be {string}")
    public void firstHeldItemShouldBe(String item) throws Exception {
        mockMvc.perform(get("/api/soap/pokemon/pikachu/held-items"))
                .andExpect(jsonPath("$.held_items[0].name", is(item)));
    }

    @And("the response should contain id value {int}")
    public void responseContainsIdValue(int id) throws Exception {
        mockMvc.perform(get("/api/soap/pokemon/pikachu/id"))
                .andExpect(jsonPath("$.id", is(id)));
    }

    @And("the response {string} should contain URL {string} into array {string}")
    public void responseContainsUrl(String path, String url, String field) throws Exception {
        String expression = String.format("$.%s[0].url", field);
        mockMvc.perform(get("/api/soap/pokemon/pikachu/" + path))
                .andExpect(jsonPath(expression, is(url)));
    }

    @And("the response should contain location value {string}")
    public void responseContainsLocationValue(String location) throws Exception {
        mockMvc.perform(get("/api/soap/pokemon/pikachu/locations"))
                .andExpect(jsonPath("$.location_area_encounters", is(location)));
    }

    @And("the response should contain name value {string}")
    public void responseContainsNameValue(String name) throws Exception {
        mockMvc.perform(get("/api/soap/pokemon/pikachu/name"))
                .andExpect(jsonPath("$.name", is(name)));
    }
}
