package com.bankaya.pokemon.infrastructure.adapter.rest.client;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.bankaya.pokemon.domain.exception.BadRequestException;
import com.bankaya.pokemon.domain.exception.PokemonNotFoundException;
import com.bankaya.pokemon.domain.exception.PokemonServiceException;
import com.bankaya.pokemon.domain.model.Pokemon;
import com.bankaya.pokemon.infrastructure.adapter.rest.dto.PokemonApiResponse;

import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokeApiClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private PokeApiClient pokeApiClient;

    private PokemonApiResponse pikachuResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pokeApiClient, "pokeApiBaseUrl", "https://pokeapi.co/api/v2");

        pikachuResponse = new PokemonApiResponse(
                25L,
                "pikachu",
                112,
                List.of(
                        new PokemonApiResponse.AbilitySlot(
                                new PokemonApiResponse.Ability("static", "https://pokeapi.co/api/v2/ability/9/"),
                                false,
                                1
                        ),
                        new PokemonApiResponse.AbilitySlot(
                                new PokemonApiResponse.Ability("lightning-rod",
                                        "https://pokeapi.co/api/v2/ability/31/"),
                                true,
                                3
                        )
                ),
                List.of(
                        new PokemonApiResponse.HeldItemSlot(
                                new PokemonApiResponse.HeldItem("light-ball", "https://pokeapi.co/api/v2/item/213/")
                        )
                ),
                "https://pokeapi.co/api/v2/pokemon/25/encounters"
        );
    }

    @Test
    void fetchPokemonByName_shouldReturnPokemon_whenSuccessful() {
        setupMockWebClient(pikachuResponse);

        Pokemon result = pokeApiClient.fetchPokemonByName("pikachu");

        assertNotNull(result);
        assertEquals(25L, result.id());
        assertEquals("pikachu", result.name());
        assertEquals(112, result.baseExperience());
        assertEquals(2, result.abilities().size());
        assertEquals("static", result.abilities().getFirst().name());
        assertEquals("lightning-rod", result.abilities().get(1).name());
        assertEquals(1, result.heldItems().size());
        assertEquals("light-ball", result.heldItems().getFirst().name());
        assertEquals("https://pokeapi.co/api/v2/pokemon/25/encounters", result.locationAreaEncounters());

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(anyString(), eq("pikachu"));
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).onStatus(any(), any());
        verify(responseSpec).bodyToMono(PokemonApiResponse.class);
    }

    @Test
    void fetchPokemonByName_shouldConvertToLowercase() {
        setupMockWebClient(pikachuResponse);

        Pokemon result = pokeApiClient.fetchPokemonByName("PIKACHU");

        assertNotNull(result);
        assertEquals("pikachu", result.name());

        verify(requestHeadersUriSpec).uri(anyString(), eq("pikachu"));
    }

    @Test
    void fetchPokemonByName_shouldHandleMixedCase() {
        setupMockWebClient(pikachuResponse);

        Pokemon result = pokeApiClient.fetchPokemonByName("PiKaChU");

        assertNotNull(result);
        verify(requestHeadersUriSpec).uri(anyString(), eq("pikachu"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void fetchPokemonByName_shouldThrowBadRequestException_whenNameIsInvalid(String invalidName) {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> pokeApiClient.fetchPokemonByName(invalidName)
        );

        assertEquals("Pokemon name cannot be null or empty", exception.getMessage());
        verify(webClient, never()).get();
    }

    @Test
    void fetchPokemonByName_shouldThrowPokemonNotFoundException_when404() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PokemonApiResponse.class))
                .thenReturn(Mono.error(new PokemonNotFoundException("unknown")));

        PokemonNotFoundException exception = assertThrows(
                PokemonNotFoundException.class,
                () -> pokeApiClient.fetchPokemonByName("unknown")
        );

        assertTrue(exception.getMessage().contains("unknown"));
    }

    @Test
    void fetchPokemonByName_shouldThrowPokemonServiceException_whenResponseIsNull() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        // Mono.empty().block() returns null, not throws exception
        when(responseSpec.bodyToMono(PokemonApiResponse.class))
                .thenReturn(Mono.justOrEmpty(Optional.empty()));

        PokemonServiceException exception = assertThrows(
                PokemonServiceException.class,
                () -> pokeApiClient.fetchPokemonByName("test")
        );

        assertEquals("Error fetching Pokemon from PokeAPI", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Empty response from PokeAPI", exception.getCause().getMessage());
    }

    @Test
    void fetchPokemonByName_shouldThrowPokemonServiceException_whenWebClientThrowsException() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PokemonApiResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Connection timeout")));

        PokemonServiceException exception = assertThrows(
                PokemonServiceException.class,
                () -> pokeApiClient.fetchPokemonByName("pikachu")
        );

        assertEquals("Error fetching Pokemon from PokeAPI", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Connection timeout", exception.getCause().getMessage());
    }

    @Test
    void fetchPokemonByName_shouldHandleWebClientResponseException() {
        WebClientResponseException webClientException = WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                null,
                null,
                null
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PokemonApiResponse.class))
                .thenReturn(Mono.error(webClientException));

        PokemonServiceException exception = assertThrows(
                PokemonServiceException.class,
                () -> pokeApiClient.fetchPokemonByName("pikachu")
        );

        assertEquals("Error fetching Pokemon from PokeAPI", exception.getMessage());
    }

    @Test
    void fetchPokemonByName_shouldMapAllAbilities() {
        PokemonApiResponse multiAbilityResponse = new PokemonApiResponse(
                1L,
                "bulbasaur",
                64,
                List.of(
                        new PokemonApiResponse.AbilitySlot(
                                new PokemonApiResponse.Ability("overgrow", "https://pokeapi.co/api/v2/ability/65/"),
                                false,
                                1
                        ),
                        new PokemonApiResponse.AbilitySlot(
                                new PokemonApiResponse.Ability("chlorophyll", "https://pokeapi.co/api/v2/ability/34/"),
                                true,
                                3
                        )
                ),
                List.of(),
                "https://pokeapi.co/api/v2/pokemon/1/encounters"
        );

        setupMockWebClient(multiAbilityResponse);

        Pokemon result = pokeApiClient.fetchPokemonByName("bulbasaur");

        assertNotNull(result);
        assertEquals(2, result.abilities().size());
        assertEquals("overgrow", result.abilities().getFirst().name());
        assertFalse(result.abilities().getFirst().isHidden());
        assertEquals(1, result.abilities().getFirst().slot());
        assertEquals("chlorophyll", result.abilities().get(1).name());
        assertTrue(result.abilities().get(1).isHidden());
        assertEquals(3, result.abilities().get(1).slot());
    }

    @Test
    void fetchPokemonByName_shouldHandleEmptyAbilitiesList() {
        PokemonApiResponse noAbilitiesResponse = new PokemonApiResponse(
                999L,
                "test-pokemon",
                0,
                List.of(),
                List.of(),
                "https://pokeapi.co/api/v2/pokemon/999/encounters"
        );

        setupMockWebClient(noAbilitiesResponse);

        Pokemon result = pokeApiClient.fetchPokemonByName("test-pokemon");

        assertNotNull(result);
        assertNotNull(result.abilities());
        assertTrue(result.abilities().isEmpty());
    }

    @Test
    void fetchPokemonByName_shouldMapAllHeldItems() {
        PokemonApiResponse multiItemResponse = new PokemonApiResponse(
                25L,
                "pikachu",
                112,
                List.of(),
                List.of(
                        new PokemonApiResponse.HeldItemSlot(
                                new PokemonApiResponse.HeldItem("light-ball", "https://pokeapi.co/api/v2/item/213/")
                        ),
                        new PokemonApiResponse.HeldItemSlot(
                                new PokemonApiResponse.HeldItem("oran-berry", "https://pokeapi.co/api/v2/item/132/")
                        )
                ),
                "https://pokeapi.co/api/v2/pokemon/25/encounters"
        );

        setupMockWebClient(multiItemResponse);

        Pokemon result = pokeApiClient.fetchPokemonByName("pikachu");

        assertNotNull(result);
        assertEquals(2, result.heldItems().size());
        assertEquals("light-ball", result.heldItems().get(0).name());
        assertEquals("https://pokeapi.co/api/v2/item/213/", result.heldItems().get(0).url());
        assertEquals("oran-berry", result.heldItems().get(1).name());
        assertEquals("https://pokeapi.co/api/v2/item/132/", result.heldItems().get(1).url());
    }

    @Test
    void fetchPokemonByName_shouldHandleEmptyHeldItemsList() {
        PokemonApiResponse noItemsResponse = new PokemonApiResponse(
                1L,
                "bulbasaur",
                64,
                List.of(),
                List.of(),
                "https://pokeapi.co/api/v2/pokemon/1/encounters"
        );

        setupMockWebClient(noItemsResponse);

        Pokemon result = pokeApiClient.fetchPokemonByName("bulbasaur");

        assertNotNull(result);
        assertNotNull(result.heldItems());
        assertTrue(result.heldItems().isEmpty());
    }

    @Test
    void fetchPokemonByName_shouldHandleNullBaseExperience() {
        PokemonApiResponse nullBaseExpResponse = new PokemonApiResponse(
                1L,
                "test",
                null,
                List.of(),
                List.of(),
                "https://pokeapi.co/api/v2/pokemon/1/encounters"
        );

        setupMockWebClient(nullBaseExpResponse);

        Pokemon result = pokeApiClient.fetchPokemonByName("test");

        assertNotNull(result);
        assertNull(result.baseExperience());
    }

    @Test
    void fetchPokemonByName_shouldHandleWhitespaceInName() {
        setupMockWebClient(pikachuResponse);

        Pokemon result = pokeApiClient.fetchPokemonByName("  pikachu  ");

        assertNotNull(result);
        verify(requestHeadersUriSpec).uri(anyString(), eq("pikachu"));
    }

    private void setupMockWebClient(PokemonApiResponse response) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PokemonApiResponse.class)).thenReturn(Mono.just(response));
    }
}
