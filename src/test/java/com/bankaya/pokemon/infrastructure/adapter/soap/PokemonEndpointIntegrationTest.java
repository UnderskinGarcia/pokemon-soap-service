package com.bankaya.pokemon.infrastructure.adapter.soap;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.ws.test.server.RequestCreators;
import org.springframework.ws.test.server.ResponseMatchers;
import org.springframework.xml.transform.StringSource;

import lombok.extern.log4j.Log4j2;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for PokemonEndpoint SOAP Web Service
 * Tests the complete flow from SOAP request to response including:
 * - XML marshalling/unmarshalling
 * - Spring WS endpoint mapping
 * - Integration with PokemonService
 * - Integration with PokeAPI client
 */
@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pokeapi.base-url=https://pokeapi.co/api/v2"
})
@Tag("integration")
@DisplayName("Pokemon SOAP Endpoint Integration Tests")
class PokemonEndpointIntegrationTest {

    private static final String NAMESPACE_URI = "http://bankaya.com/pokemon/soap";
    private static final Map<String, String> NAMESPACE_MAP = Collections.singletonMap("ns", NAMESPACE_URI);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestRestTemplate restTemplate;

    private MockWebServiceClient mockClient;

    @BeforeEach
    void setUp() {
        mockClient = MockWebServiceClient.createClient(applicationContext);
    }

    // Provider method for successful endpoint tests
    static Stream<Arguments> provideSuccessfulEndpointTestCases() {
        return Stream.of(
                Arguments.of("GetPokemonAbilitiesRequest", "pikachu", "//ns:abilities/ns:name", null),
                Arguments.of("GetPokemonBaseExperienceRequest", "pikachu", "//ns:baseExperience", "112"),
                Arguments.of("GetPokemonHeldItemsRequest", "pikachu", "//ns:GetPokemonHeldItemsResponse", null),
                Arguments.of("GetPokemonIdRequest", "pikachu", "//ns:id", "25"),
                Arguments.of("GetPokemonNameRequest", "pikachu", "//ns:name", "pikachu"),
                Arguments.of("GetPokemonLocationAreaEncountersRequest", "pikachu", "//ns:locationAreaEncounters", null)
        );
    }

    @ParameterizedTest(name = "{0} for {1} should return valid response")
    @MethodSource("provideSuccessfulEndpointTestCases")
    @DisplayName("Should successfully handle SOAP requests for various endpoints")
    void testSuccessfulEndpointRequests(String requestType, String pokemonName, String xpathExpression,
                                        String expectedValue) {
        // Given - SOAP request for the specified endpoint
        String soapRequest = String.format("""
                <%s xmlns="http://bankaya.com/pokemon/soap">
                    <name>%s</name>
                </%s>
                """, requestType, pokemonName, requestType);

        // When & Then - Send request and verify response
        var responseActions = mockClient
                .sendRequest(RequestCreators.withPayload(new StringSource(soapRequest)))
                .andExpect(ResponseMatchers.noFault())
                .andExpect(ResponseMatchers.xpath(xpathExpression, NAMESPACE_MAP).exists());

        // If expected value is provided, verify it matches
        if (expectedValue != null) {
            responseActions.andExpect(
                    ResponseMatchers.xpath(xpathExpression, NAMESPACE_MAP).evaluatesTo(expectedValue));
        }
    }

    @ParameterizedTest(name = "{0} with {1} should return SOAP fault")
    @CsvSource({
            "GetPokemonNameRequest, nonexistentpokemon123456, Pokemon not found",
            "GetPokemonNameRequest, '', Empty Pokemon name"
    })
    @DisplayName("Should handle error cases with SOAP faults")
    void testErrorCases(String requestType, String pokemonName, String testDescription) {
        // Given - SOAP request with invalid input
        String soapRequest = String.format("""
                <%s xmlns="http://bankaya.com/pokemon/soap">
                    <name>%s</name>
                </%s>
                """, requestType, pokemonName, requestType);

        // When & Then - Send request and expect a SOAP fault
        mockClient
                .sendRequest(RequestCreators.withPayload(new StringSource(soapRequest)))
                .andExpect(ResponseMatchers.serverOrReceiverFault());
    }

    @Test
    @DisplayName("Should verify WSDL is accessible")
    void testWsdlGeneration() {
        // Verify the application context and SOAP endpoint bean
        assertNotNull(applicationContext);
        assertTrue(applicationContext.containsBean("pokemon"));

        // Verify the WSDL is accessible via HTTP endpoint
        // Using relative path with TestRestTemplate (works in CI/CD pipelines)
        ResponseEntity<String> response = restTemplate.getForEntity("/pokemon/ws/pokemon.wsdl", String.class);

        assertSame(HttpStatus.OK, response.getStatusCode(), "WSDL endpoint should return HTTP 200");
        assertNotNull(response.getBody(), "WSDL content should not be null");
        assertTrue(response.getBody().contains("definitions"), "WSDL should contain 'definitions' element");
        assertTrue(response.getBody().contains("pokemon"), "WSDL should contain 'pokemon' service definition");
    }

    @Test
    @DisplayName("Should handle case-insensitive Pokemon names correctly")
    void testCaseInsensitivePokemonNames() {
        // Given - SOAP request with uppercase name (service converts to lowercase)
        String soapRequest = """
                <GetPokemonIdRequest xmlns="http://bankaya.com/pokemon/soap">
                    <name>PIKACHU</name>
                </GetPokemonIdRequest>
                """;

        // When & Then - Service should handle it gracefully by converting to lowercase
        mockClient
                .sendRequest(RequestCreators.withPayload(new StringSource(soapRequest)))
                .andExpect(ResponseMatchers.noFault())
                .andExpect(ResponseMatchers.xpath("//ns:id", NAMESPACE_MAP).exists())
                .andExpect(ResponseMatchers.xpath("//ns:id", NAMESPACE_MAP).evaluatesTo("25"));
    }

    @Test
    @DisplayName("Should get abilities for Pokemon with multiple abilities")
    void testMultipleAbilities() {
        // Given - Request for a Pokemon with multiple abilities (Pikachu has 2)
        String soapRequest = """
                <GetPokemonAbilitiesRequest xmlns="http://bankaya.com/pokemon/soap">
                    <name>pikachu</name>
                </GetPokemonAbilitiesRequest>
                """;

        // When & Then - Verify multiple abilities are returned
        mockClient
                .sendRequest(RequestCreators.withPayload(new StringSource(soapRequest)))
                .andExpect(ResponseMatchers.noFault())
                .andExpect(ResponseMatchers.xpath("count(//ns:abilities)", NAMESPACE_MAP).evaluatesTo(2));
    }
}
