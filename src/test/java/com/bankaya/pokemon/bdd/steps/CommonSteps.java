package com.bankaya.pokemon.bdd.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.bankaya.pokemon.bdd.context.ScenarioContext;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Basic step definitions for simple Cucumber tests
 */
public class CommonSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScenarioContext scenarioContext;

    @Given("the REST API is available")
    public void restApiIsAvailable() {
        if (mockMvc == null) {
            throw new RuntimeException("MockMvc is not available!");
        }
        System.out.println("✓ MockMvc is available");
    }

    @When("I call GET {string}")
    public void callGetEndpoint(String endpoint) throws Exception {
        System.out.println("Calling GET " + endpoint);
        var result = mockMvc.perform(get(endpoint)).andReturn();
        scenarioContext.setLastResponse(result);
        System.out.println("Response status: " + result.getResponse().getStatus());
    }

    @Then("the response status should be {int}")
    public void responseStatusIs(int expectedStatus) {
        var lastResponse = scenarioContext.getLastResponse();
        if (lastResponse == null) {
            throw new IllegalStateException("No response available. Did you call the endpoint first?");
        }

        int actualStatus = lastResponse.getResponse().getStatus();
        if (actualStatus != expectedStatus) {
            throw new AssertionError(
                    String.format("Expected status %d but got %d", expectedStatus, actualStatus)
            );
        }
        System.out.println("✓ Response status " + actualStatus + " is correct");
    }
}