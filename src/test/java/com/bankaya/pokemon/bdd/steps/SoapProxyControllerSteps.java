package com.bankaya.pokemon.bdd.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.bankaya.pokemon.application.service.PokemonService;
import com.bankaya.pokemon.bdd.context.ScenarioContext;
import com.bankaya.pokemon.domain.exception.PokemonNotFoundException;
import com.bankaya.pokemon.soap.Ability;
import com.bankaya.pokemon.soap.GetPokemonAbilitiesResponse;
import com.bankaya.pokemon.soap.GetPokemonBaseExperienceResponse;
import com.bankaya.pokemon.soap.GetPokemonHeldItemsResponse;
import com.bankaya.pokemon.soap.GetPokemonIdResponse;
import com.bankaya.pokemon.soap.GetPokemonNameResponse;
import com.bankaya.pokemon.soap.HeldItem;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Step definitions for SOAP Proxy Controller BDD tests
 */
public class SoapProxyControllerSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PokemonService pokemonService;

    @Autowired
    private ScenarioContext scenarioContext;

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

        when(pokemonService.getPokemonAbilities(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(response);
    }

    @Given("I have mocked Pokemon {string} with base experience {int}")
    public void mockPokemonWithBaseExperience(String pokemonName, int baseExp) {
        GetPokemonBaseExperienceResponse response = new GetPokemonBaseExperienceResponse();
        response.setBaseExperience(baseExp);
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

        when(pokemonService.getPokemonHeldItems(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(response);
    }

    @Given("I have mocked Pokemon {string} with expectedId {int}")
    public void iHaveMockedPokemonWithId(String pokemonName, int expectedId) {
        GetPokemonIdResponse response = new GetPokemonIdResponse();
        response.setId(expectedId);

        when(pokemonService.getPokemonId(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(response);
    }

    @Given("I have mocked Pokemon {string} with ID {int}")
    public void mockPokemonWithId(String pokemonName, long id) {
        GetPokemonIdResponse response = new GetPokemonIdResponse();
        response.setId(id);

        when(pokemonService.getPokemonId(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(response);
    }

    @Given("I have mocked Pokemon {string} with name {string}")
    public void mockPokemonWithName(String pokemonName, String name) {
        GetPokemonNameResponse response = new GetPokemonNameResponse();
        response.setName(name);
        when(pokemonService.getPokemonName(
                argThat(arg -> arg != null && arg.equalsIgnoreCase(pokemonName))
        )).thenReturn(response);
    }

    @Given("the Pokemon {string} does not exist")
    public void pokemonDoesNotExist(String pokemonName) {
        when(pokemonService.getPokemonId(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenThrow(new PokemonNotFoundException(pokemonName));
    }

    @Given("I have an empty Pokemon name")
    public void emptyPokemonName() {
        // Empty name will be handled by the endpoint
    }

    // Todos los @And ahora usan el contexto compartido
    @And("the response should contain base_experience value {int}")
    public void responseContainsBaseExperience(int baseExp) throws Exception {
        var response = scenarioContext.getLastResponse();
        jsonPath("$.base_experience", is(baseExp)).match(response);
    }

    @And("the response should contain {int} abilities")
    public void responseContainsAbilities(int number) throws Exception {
        var response = scenarioContext.getLastResponse();
        jsonPath("$.abilities.length()", is(number)).match(response);
    }

    @And("the response should contain {int} held item")
    public void responseContainsHeldItems(int number) throws Exception {
        var response = scenarioContext.getLastResponse();
        jsonPath("$.held_items.length()", is(number)).match(response);
    }

    @And("the first ability should be {string}")
    public void firstAbilityShouldBe(String ability) throws Exception {
        var response = scenarioContext.getLastResponse();
        jsonPath("$.abilities[0].name", is(ability)).match(response);
    }

    @And("the first held item should be {string}")
    public void firstHeldItemShouldBe(String item) throws Exception {
        var response = scenarioContext.getLastResponse();
        jsonPath("$.held_items[0].name", is(item)).match(response);
    }

    @And("the response should contain id value {int}")
    public void responseContainsIdValue(int id) throws Exception {
        var response = scenarioContext.getLastResponse();
        jsonPath("$.id", is(id)).match(response);
    }

    @And("the response {string} should contain URL {string} into array {string}")
    public void responseContainsUrl(String path, String url, String field) throws Exception {
        var response = scenarioContext.getLastResponse();
        String expression = String.format("$.%s[0].url", field);
        jsonPath(expression, is(url)).match(response);
    }

    @And("the response should contain location value {string}")
    public void responseContainsLocationValue(String location) throws Exception {
        var response = scenarioContext.getLastResponse();
        jsonPath("$.location_area_encounters", is(location)).match(response);
    }

    @And("the response should contain name value {string}")
    public void responseContainsNameValue(String name) throws Exception {
        var response = scenarioContext.getLastResponse();
        jsonPath("$.name", is(name)).match(response);
    }
}