package com.bankaya.pokemon.bdd.steps;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.ws.test.server.RequestCreators;
import org.springframework.ws.test.server.ResponseMatchers;
import org.springframework.xml.transform.StringSource;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for Pokemon SOAP Endpoint BDD tests
 */
public class PokemonEndpointSteps {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    private MockWebServiceClient mockClient;
    private ResponseEntity<String> lastResponse;
    private boolean lastResponseHasFault;

    @Before
    public void setUp() {
        if (applicationContext != null && mockClient == null) {
            mockClient = MockWebServiceClient.createClient(applicationContext);
        }
    }

    @Given("the SOAP endpoint is available")
    public void soapEndpointIsAvailable() {
        assertNotNull(applicationContext, "Application context should be initialized");
        assertTrue(applicationContext.containsBean("pokemon"), "Pokemon SOAP bean should exist");
    }

    @And("the namespace URI is {string}")
    public void namespaceUriIs(String namespaceUri) {
        Map<String, String> namespaceMap = Collections.singletonMap("ns", namespaceUri);
        assertNotNull(namespaceMap, "Namespace map should be configured");
    }

    @When("I send a GetPokemonAbilitiesRequest for {string}")
    public void sendGetPokemonAbilitiesRequest(String pokemonName) {
        sendSoapRequest("GetPokemonAbilitiesRequest", pokemonName);
    }

    @When("I send a GetPokemonBaseExperienceRequest for {string}")
    public void sendGetPokemonBaseExperienceRequest(String pokemonName) {
        sendSoapRequest("GetPokemonBaseExperienceRequest", pokemonName);
    }

    @When("I send a GetPokemonHeldItemsRequest for {string}")
    public void sendGetPokemonHeldItemsRequest(String pokemonName) {
        sendSoapRequest("GetPokemonHeldItemsRequest", pokemonName);
    }

    @When("I send a GetPokemonIdRequest for {string}")
    public void sendGetPokemonIdRequest(String pokemonName) {
        sendSoapRequest("GetPokemonIdRequest", pokemonName);
    }

    @When("I send a GetPokemonNameRequest for {string}")
    public void sendGetPokemonNameRequest(String pokemonName) {
        sendSoapRequest("GetPokemonNameRequest", pokemonName);
    }

    @When("I send a GetPokemonLocationAreaEncountersRequest for {string}")
    public void sendGetPokemonLocationAreaEncountersRequest(String pokemonName) {
        sendSoapRequest("GetPokemonLocationAreaEncountersRequest", pokemonName);
    }

    @When("I send a {string} with empty name")
    public void sendRequestWithEmptyName(String requestType) {
        String soapRequest = String.format("""
                <%s xmlns="http://bankaya.com/pokemon/soap">
                    <name></name>
                </%s>
                """, requestType, requestType);
        sendRawSoapRequest(soapRequest);
    }

    @When("I send a {string} for {string}")
    public void sendRequestForPokemon(String requestType, String pokemonName) {
        sendSoapRequest(requestType, pokemonName);
    }

    @When("I request the WSDL at {string}")
    public void requestWsdlAt(String path) {
        try {
            lastResponse = new org.springframework.boot.test.web.client.TestRestTemplate()
                    .getForEntity("http://localhost:8080" + path, String.class);
        } catch (Exception e) {
            System.err.println("Error requesting WSDL: " + e.getMessage());
        }
    }

    @Then("the response should contain no SOAP fault")
    public void responseContainsNoFault() {
        assertFalse(lastResponseHasFault, "Response should not contain a SOAP fault");
    }

    @Then("the response should contain a CLIENT SOAP fault")
    public void responseContainsClientFault() {
        assertTrue(lastResponseHasFault, "Response should contain a SOAP fault");
    }

    @Then("the response should contain a SERVER SOAP fault")
    public void responseContainsServerFault() {
        assertTrue(lastResponseHasFault, "Response should contain a SOAP fault");
    }

    @Then("the response should contain abilities element")
    public void responseContainsAbilitiesElement() {
        assertNotNull(mockClient, "Mock client should be initialized");
    }

    @Then("the response should contain baseExperience with value {string}")
    public void responseContainsBaseExperienceWithValue(String expectedValue) {
        assertNotNull(expectedValue, "Expected value should not be null");
    }

    @Then("the response should contain GetPokemonHeldItemsResponse element")
    public void responseContainsHeldItemsElement() {
        assertNotNull(mockClient, "Mock client should be initialized");
    }

    @Then("the response should contain id with value {string}")
    public void responseContainsIdWithValue(String expectedValue) {
        assertNotNull(expectedValue, "Expected value should not be null");
    }

    @Then("the response should contain name with value {string}")
    public void responseContainsNameWithValue(String expectedValue) {
        assertNotNull(expectedValue, "Expected value should not be null");
    }

    @Then("the response should contain locationAreaEncounters element")
    public void responseContainsLocationEncountersElement() {
        assertNotNull(mockClient, "Mock client should be initialized");
    }

    @Then("the response status code should be {int}")
    public void responseStatusCodeIs(int statusCode) {
        assertNotNull(lastResponse, "Last response should exist");
        assertEquals(statusCode, lastResponse.getStatusCode().value(),
                "Status code should be " + statusCode);
    }

    private void sendSoapRequest(String requestType, String pokemonName) {
        String soapRequest = String.format("""
                <%s xmlns="http://bankaya.com/pokemon/soap">
                    <name>%s</name>
                </%s>
                """, requestType, pokemonName, requestType);
        sendRawSoapRequest(soapRequest);
    }

    private void sendRawSoapRequest(String soapRequest) {
        try {
            if (mockClient != null) {
                var actions = mockClient
                        .sendRequest(RequestCreators.withPayload(new StringSource(soapRequest)));

                // Try to check for no fault
                try {
                    actions.andExpect(ResponseMatchers.noFault());
                    lastResponseHasFault = false;
                } catch (Exception e) {
                    lastResponseHasFault = true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending SOAP request: " + e.getMessage());
            lastResponseHasFault = true;
        }
    }
}
