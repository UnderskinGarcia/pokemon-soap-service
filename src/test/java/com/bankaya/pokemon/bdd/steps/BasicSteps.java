package com.bankaya.pokemon.bdd.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Basic step definitions for simple Cucumber tests
 */
public class BasicSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult lastResponse;

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
        lastResponse = mockMvc.perform(get(endpoint)).andReturn();
        System.out.println("Response status: " + lastResponse.getResponse().getStatus());
    }

    @Then("the response status should be {int}")
    public void responseStatusIs(int expectedStatus) {
        int actualStatus = lastResponse.getResponse().getStatus();
        if (actualStatus != expectedStatus) {
            throw new AssertionError(
                    String.format("Expected status %d but got %d", expectedStatus, actualStatus)
            );
        }
        System.out.println("✓ Response status " + actualStatus + " is correct");
    }
}
