package com.bankaya.pokemon.bdd.steps;

import java.util.List;
import java.util.Map;

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
import com.bankaya.pokemon.soap.GetPokemonLocationAreaEncountersResponse;
import com.bankaya.pokemon.soap.GetPokemonNameResponse;
import com.bankaya.pokemon.soap.HeldItem;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;

import lombok.extern.log4j.Log4j2;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Step definitions for Pokemon REST Controller BDD tests
 */
@Log4j2
public class PokemonRestControllerSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PokemonService pokemonService;

    @Autowired
    private ScenarioContext scenarioContext;

    // Setup steps
    @Given("the Pokemon REST API is available")
    public void pokemonRestApiIsAvailable() {
        assertNotNull(mockMvc, "MockMvc should be available");
        log.info("✓ Pokemon REST API is available");
    }

    @Given("the Pokemon API client is mocked")
    public void pokemonApiClientIsMocked() {
        // Service is already mocked via @MockitoBean in CucumberSpringConfiguration
        log.info("✓ Pokemon API client is mocked");
    }

    // Mock data setup
    @Given("I have mocked Pokemon data for {string} with id {int}")
    public void mockPokemonWithId(String pokemonName, int id) {
        int baseExp = getDefaultBaseExperience(pokemonName);
        setupPokemonMocks(pokemonName, id, baseExp);
        log.info("✓ Mocked Pokemon {} with id {}", pokemonName, id);
    }

    @Given("the Pokemon {string} does not exist in the API")
    public void pokemonDoesNotExistInApi(String pokemonName) {
        when(pokemonService.getPokemonId(pokemonName))
                .thenThrow(new PokemonNotFoundException(pokemonName));

        when(pokemonService.getPokemonName(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenThrow(new PokemonNotFoundException(pokemonName));

        when(pokemonService.getPokemonAbilities(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenThrow(new PokemonNotFoundException(pokemonName));

        log.info("✓ Mocked Pokemon {} as not existing", pokemonName);
    }

    @Given("I have mocked Pokemon {string} with {int} abilities:")
    public void mockPokemonWithAbilitiesTable(String pokemonName, int count, List<Map<String, String>> abilities) {
        int id = getDefaultId(pokemonName);
        int baseExp = getDefaultBaseExperience(pokemonName);
        setupPokemonMocks(pokemonName, id, baseExp);

        // Mock abilities
        GetPokemonAbilitiesResponse abilitiesResponse = new GetPokemonAbilitiesResponse();
        for (int i = 0; i < abilities.size(); i++) {
            Map<String, String> abilityData = abilities.get(i);
            Ability ability = new Ability();
            ability.setName(abilityData.get("ability"));
            ability.setUrl(abilityData.get("url"));
            ability.setSlot(i + 1);
            ability.setIsHidden(false);
            abilitiesResponse.getAbilities().add(ability);
        }

        when(pokemonService.getPokemonAbilities(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(abilitiesResponse);

        log.info("✓ Mocked Pokemon {} with {} abilities", pokemonName, count);
    }

    @Given("I have mocked Pokemon {string} with held item:")
    public void mockPokemonWithHeldItemTable(String pokemonName, List<Map<String, String>> items) {
        int id = getDefaultId(pokemonName);
        int baseExp = getDefaultBaseExperience(pokemonName);
        setupPokemonMocks(pokemonName, id, baseExp);

        // Mock held items
        GetPokemonHeldItemsResponse heldItemsResponse = new GetPokemonHeldItemsResponse();
        for (Map<String, String> itemData : items) {
            HeldItem item = new HeldItem();
            item.setName(itemData.get("name"));
            item.setUrl(itemData.get("url"));
            heldItemsResponse.getHeldItems().add(item);
        }

        when(pokemonService.getPokemonHeldItems(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(heldItemsResponse);

        log.info("✓ Mocked Pokemon {} with held items", pokemonName);
    }

    @Given("I have mocked Pokemon {string} with location encounters {string}")
    public void mockPokemonWithLocationEncounters(String pokemonName, String encountersUrl) {
        int id = getDefaultId(pokemonName);
        int baseExp = getDefaultBaseExperience(pokemonName);
        setupPokemonMocks(pokemonName, id, baseExp);

        // Mock location encounters
        GetPokemonLocationAreaEncountersResponse encountersResponse = new GetPokemonLocationAreaEncountersResponse();
        encountersResponse.setLocationAreaEncounters(encountersUrl);

        when(pokemonService.getPokemonLocationAreaEncounters(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(encountersResponse);

        log.info("✓ Mocked Pokemon {} with location encounters: {}", pokemonName, encountersUrl);
    }

    @And("the response should be in JSON format")
    public void responseIsJsonFormat() {
        var response = scenarioContext.getLastResponse();
        String contentType = response.getResponse().getContentType();
        assertTrue(contentType != null && contentType.contains("application/json"),
                "Response should be in JSON format");
        log.info("✓ Response is in JSON format");
    }

    @And("the response should contain abilities array")
    public void responseContainsAbilitiesArray() throws Exception {
        var response = scenarioContext.getLastResponse();
        jsonPath("$.abilities").exists().match(response);
        log.info("✓ Response contains abilities array");
    }

    @And("the second ability should be {string}")
    public void secondAbilityShouldBe(String ability) throws Exception {
        var response = scenarioContext.getLastResponse();
        jsonPath("$.abilities[1].name", is(ability)).match(response);
        log.info("✓ Second ability is {}", ability);
    }

    @And("the held item should be {string}")
    public void heldItemShouldBe(String itemName) throws Exception {
        var response = scenarioContext.getLastResponse();
        jsonPath("$.held_items[0].name", is(itemName)).match(response);
        log.info("✓ Held item is {}", itemName);
    }

    @And("the response should contain location_area_encounters value {string}")
    public void responseContainsLocationAreaEncountersValue(String url) throws Exception {
        var response = scenarioContext.getLastResponse();
        jsonPath("$.location_area_encounters", is(url)).match(response);
        log.info("✓ Response contains location_area_encounters: {}", url);
    }

    // Note: "the response should contain {string}" is inherited from PokemonEndpointSteps

    // Helper methods
    private int getDefaultId(String pokemonName) {
        return switch (pokemonName.toLowerCase()) {
            case "pikachu" -> 25;
            case "bulbasaur" -> 1;
            case "charmander" -> 4;
            case "squirtle" -> 7;
            default -> 1;
        };
    }

    private int getDefaultBaseExperience(String pokemonName) {
        return switch (pokemonName.toLowerCase()) {
            case "pikachu" -> 112;
            case "bulbasaur" -> 64;
            case "charmander" -> 62;
            case "squirtle" -> 63;
            default -> 64;
        };
    }

    private void setupPokemonMocks(String pokemonName, int id, int baseExp) {
        // Mock ID response
        GetPokemonIdResponse idResponse = new GetPokemonIdResponse();
        idResponse.setId(id);
        when(pokemonService.getPokemonId(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(idResponse);

        // Mock name response
        GetPokemonNameResponse nameResponse = new GetPokemonNameResponse();
        nameResponse.setName(pokemonName.toLowerCase());
        when(pokemonService.getPokemonName(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(nameResponse);

        // Mock base experience response
        GetPokemonBaseExperienceResponse baseExpResponse = new GetPokemonBaseExperienceResponse();
        baseExpResponse.setBaseExperience(baseExp);
        when(pokemonService.getPokemonBaseExperienceResponse(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(baseExpResponse);

        // Mock abilities response (default 2 abilities)
        GetPokemonAbilitiesResponse abilitiesResponse = new GetPokemonAbilitiesResponse();
        Ability ability1 = new Ability();
        ability1.setName("static");
        ability1.setUrl("https://pokeapi.co/api/v2/ability/9/");
        ability1.setSlot(1);
        ability1.setIsHidden(false);
        abilitiesResponse.getAbilities().add(ability1);

        Ability ability2 = new Ability();
        ability2.setName("lightning-rod");
        ability2.setUrl("https://pokeapi.co/api/v2/ability/31/");
        ability2.setSlot(2);
        ability2.setIsHidden(true);
        abilitiesResponse.getAbilities().add(ability2);

        when(pokemonService.getPokemonAbilities(
                argThat(name -> name != null && name.equalsIgnoreCase(pokemonName))
        )).thenReturn(abilitiesResponse);
    }
}
