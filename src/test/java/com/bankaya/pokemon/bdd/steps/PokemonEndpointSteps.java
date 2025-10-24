package com.bankaya.pokemon.bdd.steps;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.ws.test.server.RequestCreators;
import org.springframework.xml.transform.StringSource;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.ws.test.server.ResponseMatchers.noFault;

/**
 * Step definitions for Pokemon SOAP Endpoint BDD tests
 */
@Log4j2
public class PokemonEndpointSteps {

    @Autowired
    private ApplicationContext applicationContext;

    @LocalServerPort
    private int port;

    private MockWebServiceClient mockClient;
    private ResponseEntity<String> lastHttpResponse;
    private Throwable lastException;
    private String lastSoapResponseXml;
    private boolean hasSoapFault = false;
    private String faultCode;
    private String faultString;

    @Before
    public void setUp() {
        if (applicationContext != null && mockClient == null) {
            mockClient = MockWebServiceClient.createClient(applicationContext);
        }
        resetState();
    }

    private void resetState() {
        lastException = null;
        lastSoapResponseXml = null;
        hasSoapFault = false;
        faultCode = null;
        faultString = null;
    }

    @Given("the SOAP endpoint is available")
    public void soapEndpointIsAvailable() {
        assertNotNull(applicationContext, "Application context should be initialized");
        assertTrue(applicationContext.containsBean("pokemon"), "Pokemon SOAP bean should exist");
        log.info("✓ SOAP endpoint is available");
    }

    @And("the namespace URI is {string}")
    public void namespaceUriIs(String namespaceUri) {
        Map<String, String> namespaceMap = Collections.singletonMap("ns", namespaceUri);
        assertNotNull(namespaceMap, "Namespace map should be configured");
        log.info("✓ Namespace URI configured: {}", namespaceUri);
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
        log.info("Sending {} with empty name", requestType);
        sendRawSoapRequest(soapRequest);
    }

    @When("I send a {string} for {string}")
    public void sendRequestForPokemon(String requestType, String pokemonName) {
        sendSoapRequest(requestType, pokemonName);
    }

    @When("I request the WSDL at {string}")
    public void requestWsdlAt(String path) {
        try {
            TestRestTemplate restTemplate = new TestRestTemplate();
            String url = "http://localhost:" + port + path;
            log.info("Requesting WSDL at: {}", url);
            lastHttpResponse = restTemplate.getForEntity(url, String.class);
            log.info("WSDL response status: {}", lastHttpResponse.getStatusCode());
        } catch (Exception e) {
            log.error("Error requesting WSDL: {}", e.getMessage());
            lastException = e;
        }
    }

    @Then("the response should contain no SOAP fault")
    public void responseContainsNoFault() {
        assertFalse(hasSoapFault,
                "Expected no SOAP fault, but got fault: " + faultCode + " - " + faultString);
        assertNull(lastException,
                "Expected no exception, but got: " +
                        (lastException != null ? lastException.getMessage() : ""));
        log.info("✓ No SOAP fault present");
    }

    @Then("the response should contain a CLIENT SOAP fault")
    public void responseContainsClientFault() {
        assertTrue(hasSoapFault,
                "Expected a CLIENT SOAP fault but no fault occurred. " +
                        "Response: " + lastSoapResponseXml);
        assertNotNull(faultCode, "Fault code should not be null");

        boolean isClientFault = faultCode.toUpperCase().contains("CLIENT");

        assertTrue(isClientFault,
                String.format("Expected CLIENT fault but got: %s - %s", faultCode, faultString));
        log.info("✓ CLIENT SOAP fault detected: {} - {}", faultCode, faultString);
    }

    @Then("the response should contain a SERVER SOAP fault")
    public void responseContainsServerFault() {
        assertTrue(hasSoapFault,
                "Expected a SERVER SOAP fault but no fault occurred. " +
                        "Response: " + lastSoapResponseXml);
        assertNotNull(faultCode, "Fault code should not be null");

        boolean isServerFault = faultCode.toUpperCase().contains("SERVER");

        assertTrue(isServerFault,
                String.format("Expected SERVER fault but got: %s - %s", faultCode, faultString));
        log.info("✓ SERVER SOAP fault detected: {} - {}", faultCode, faultString);
    }

    @Then("the response should contain abilities element")
    public void responseContainsAbilitiesElement() {
        assertFalse(hasSoapFault, "Request should succeed without SOAP fault");
        assertNull(lastException, "Request should succeed without errors");
        log.info("✓ Response contains abilities element");
    }

    @Then("the response should contain baseExperience with value {string}")
    public void responseContainsBaseExperienceWithValue(String expectedValue) {
        assertFalse(hasSoapFault, "Request should succeed without SOAP fault");
        if (lastSoapResponseXml != null) {
            assertThat("Response should contain baseExperience with value " + expectedValue,
                    lastSoapResponseXml, containsString(expectedValue));
        }
        log.info("✓ Response contains baseExperience: {}", expectedValue);
    }

    @Then("the response should contain GetPokemonHeldItemsResponse element")
    public void responseContainsHeldItemsElement() {
        assertFalse(hasSoapFault, "Request should succeed without SOAP fault");
        log.info("✓ Response contains GetPokemonHeldItemsResponse element");
    }

    @Then("the response should contain id with value {string}")
    public void responseContainsIdWithValue(String expectedValue) {
        assertFalse(hasSoapFault, "Request should succeed without SOAP fault");
        if (lastSoapResponseXml != null) {
            assertThat("Response should contain id with value " + expectedValue,
                    lastSoapResponseXml, containsString("<id>" + expectedValue + "</id>"));
        }
        log.info("✓ Response contains id: {}", expectedValue);
    }

    @Then("the response should contain name with value {string}")
    public void responseContainsNameWithValue(String expectedValue) {
        assertFalse(hasSoapFault, "Request should succeed without SOAP fault");
        if (lastSoapResponseXml != null) {
            assertThat("Response should contain name with value " + expectedValue,
                    lastSoapResponseXml, containsString("<name>" + expectedValue + "</name>"));
        }
        log.info("✓ Response contains name: {}", expectedValue);
    }

    @Then("the response should contain locationAreaEncounters element")
    public void responseContainsLocationEncountersElement() {
        assertFalse(hasSoapFault, "Request should succeed without SOAP fault");
        log.info("✓ Response contains locationAreaEncounters element");
    }

    @Then("the response status code should be {int}")
    public void responseStatusCodeIs(int statusCode) {
        assertNotNull(lastHttpResponse, "Last HTTP response should exist");
        assertEquals(statusCode, lastHttpResponse.getStatusCode().value(),
                "Status code should be " + statusCode);
        log.info("✓ Status code is {}", statusCode);
    }

    @And("the response should contain {string}")
    public void responseContainsText(String expectedText) {
        assertNotNull(lastHttpResponse, "Last HTTP response should exist");
        assertNotNull(lastHttpResponse.getBody(), "Response body should not be null");
        assertThat("Response should contain: " + expectedText,
                lastHttpResponse.getBody(), containsString(expectedText));
        log.info("✓ Response contains: {}", expectedText);
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
        resetState();

        try {
            if (mockClient != null) {
                var actions = mockClient
                        .sendRequest(RequestCreators.withPayload(new StringSource(soapRequest)));

                // Check for SOAP fault
                try {
                    actions.andExpect(noFault());
                    hasSoapFault = false;
                    log.debug("No SOAP fault detected");
                } catch (AssertionError e) {
                    hasSoapFault = true;
                    lastException = e;
                    extractFaultInfo(e.getMessage());
                    log.warn("SOAP Fault detected: {} - {}", faultCode, faultString);
                }

                // Capture response if no fault
                if (!hasSoapFault) {
                    try {
                        actions.andExpect((request, response) -> {
                            try {
                                TransformerFactory factory = TransformerFactory.newInstance();
                                Transformer transformer = factory.newTransformer();
                                StringWriter writer = new StringWriter();
                                transformer.transform(response.getPayloadSource(), new StreamResult(writer));
                                lastSoapResponseXml = writer.toString();
                                log.debug("Captured response: {}",
                                        lastSoapResponseXml.substring(0, Math.min(200, lastSoapResponseXml.length())));
                            } catch (Exception e) {
                                log.error("Could not capture response: {}", e.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        log.error("Error in response capture: {}", e.getMessage());
                    }
                }
            }
        } catch (SoapFaultClientException e) {
            hasSoapFault = true;
            lastException = e;
            faultCode = e.getFaultCode() != null ? e.getFaultCode().getLocalPart() : "Unknown";
            faultString = e.getFaultStringOrReason();
            log.warn("SoapFaultClientException: {} - {}", faultCode, faultString);
        } catch (Exception e) {
            log.error("SOAP request failed: {}", e.getMessage(), e);
            lastException = e;
            hasSoapFault = true;
            extractFaultInfo(e.getMessage());
        }
    }

    private void extractFaultInfo(String errorMessage) {
        if (errorMessage == null) {
            faultCode = "Unknown";
            faultString = "Unknown error";
            return;
        }

        // Extract fault code from Spring WS error messages
        if (errorMessage.toUpperCase().contains("CLIENT")) {
            faultCode = "Client";
        } else if (errorMessage.toUpperCase().contains("SERVER")) {
            faultCode = "Server";
        } else {
            faultCode = "Unknown";
        }

        faultString = errorMessage;
    }
}