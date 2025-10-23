package com.bankaya.pokemon.application.rest.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankaya.pokemon.application.rest.dto.SoapProxyResponses;
import com.bankaya.pokemon.application.rest.dto.SoapProxyResponses.AbilitiesResponse;
import com.bankaya.pokemon.application.service.PokemonService;
import com.bankaya.pokemon.soap.GetPokemonAbilitiesResponse;
import com.bankaya.pokemon.soap.GetPokemonBaseExperienceResponse;
import com.bankaya.pokemon.soap.GetPokemonHeldItemsResponse;
import com.bankaya.pokemon.soap.GetPokemonIdResponse;
import com.bankaya.pokemon.soap.GetPokemonLocationAreaEncountersResponse;
import com.bankaya.pokemon.soap.GetPokemonNameResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

/**
 * SOAP Proxy REST Controller
 * Provides REST endpoints that internally call SOAP operations
 * This allows testing SOAP functionality through Swagger UI's "Try it out" feature
 */
@Tag(
        name = "SOAP Proxy API",
        description = """
                REST endpoints that proxy SOAP operations for easy testing in Swagger UI.
                These endpoints call the same SOAP service internally, allowing you to test
                SOAP functionality through REST interface with 'Try it out' button.
                """
)
@RestController
@RequestMapping("/api/soap/pokemon")
@RequiredArgsConstructor
public class SoapProxyController {

    private final PokemonService pokemonService;

    @Operation(
            summary = "Get Pokemon abilities (SOAP proxy)",
            description = """
                    Retrieves all abilities of a Pokemon by calling the SOAP GetPokemonAbilities operation internally.
                    This is a REST wrapper that allows testing the SOAP service through Swagger UI.
                    
                    **Underlying SOAP Operation:** `GetPokemonAbilitiesRequest`
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pokemon abilities retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AbilitiesResponse.class),
                    examples = @ExampleObject(
                            name = "Pikachu abilities",
                            value = """
                                    {
                                      "abilities": [
                                        {
                                          "name": "static",
                                          "url": "https://pokeapi.co/api/v2/ability/9/",
                                          "isHidden": false,
                                          "slot": 1
                                        },
                                        {
                                          "name": "lightning-rod",
                                          "url": "https://pokeapi.co/api/v2/ability/31/",
                                          "isHidden": true,
                                          "slot": 3
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(responseCode = "400", description = "Bad request - Pokemon name is empty")
    @ApiResponse(responseCode = "404", description = "Pokemon not found")
    @GetMapping("/{name}/abilities")
    public SoapProxyResponses.AbilitiesResponse getAbilities(
            @Parameter(description = "Pokemon name (case-insensitive)", example = "pikachu", required = true)
            @PathVariable String name) {

        GetPokemonAbilitiesResponse soapResponse = pokemonService.getPokemonAbilities(name);

        List<SoapProxyResponses.AbilityDTO> abilities =
                soapResponse.getAbilities().stream()
                        .map(ability -> new SoapProxyResponses.AbilityDTO(
                                ability.getName(),
                                ability.getUrl(),
                                ability.isIsHidden(),
                                ability.getSlot()
                        ))
                        .toList();

        return new AbilitiesResponse(abilities);
    }

    @Operation(
            summary = "Get Pokemon base experience (SOAP proxy)",
            description = """
                    Retrieves the base experience points of a Pokemon.
                    
                    **Underlying SOAP Operation:** `GetPokemonBaseExperienceRequest`
                    """
    )

    @ApiResponse(
            responseCode = "200",
            description = "Base experience retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = "{\"baseExperience\": 112}")
            )
    )
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Pokemon not found")
    @GetMapping("/{name}/base-experience")
    public SoapProxyResponses.BaseExperienceResponse getBaseExperience(
            @Parameter(description = "Pokemon name", example = "pikachu", required = true)
            @PathVariable String name) {

        GetPokemonBaseExperienceResponse soapResponse = pokemonService.getPokemonBaseExperienceResponse(name);
        return new SoapProxyResponses.BaseExperienceResponse(
                soapResponse.getBaseExperience());
    }

    @Operation(
            summary = "Get Pokemon held items (SOAP proxy)",
            description = """
                    Retrieves items that a Pokemon can hold.
                    
                    **Underlying SOAP Operation:** `GetPokemonHeldItemsRequest`
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Held items retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "heldItems": [
                                        {
                                          "name": "light-ball",
                                          "url": "https://pokeapi.co/api/v2/item/213/"
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Pokemon not found")
    @GetMapping("/{name}/held-items")
    public SoapProxyResponses.HeldItemsResponse getHeldItems(
            @Parameter(description = "Pokemon name", example = "pikachu", required = true)
            @PathVariable String name) {

        GetPokemonHeldItemsResponse soapResponse = pokemonService.getPokemonHeldItems(name);

        List<SoapProxyResponses.HeldItemDTO> items =
                soapResponse.getHeldItems().stream()
                        .map(item -> new SoapProxyResponses.HeldItemDTO(
                                item.getName(),
                                item.getUrl()
                        ))
                        .toList();

        return new SoapProxyResponses.HeldItemsResponse(items);
    }

    @Operation(
            summary = "Get Pokemon ID (SOAP proxy)",
            description = """
                    Retrieves the numeric ID of a Pokemon.
                    
                    **Underlying SOAP Operation:** `GetPokemonIdRequest`
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pokemon ID retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = "{\"id\": 25}")
            )
    )
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Pokemon not found")
    @GetMapping("/{name}/id")
    public SoapProxyResponses.IdResponse getId(
            @Parameter(description = "Pokemon name", example = "pikachu", required = true)
            @PathVariable String name) {

        GetPokemonIdResponse soapResponse = pokemonService.getPokemonId(name);
        return new SoapProxyResponses.IdResponse(soapResponse.getId());
    }

    @Operation(
            summary = "Get Pokemon name (SOAP proxy)",
            description = """
                    Validates and returns the Pokemon name.
                    Useful for validating if a Pokemon exists.
                    
                    **Underlying SOAP Operation:** `GetPokemonNameRequest`
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pokemon name validated successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = "{\"name\": \"pikachu\"}")
            )
    )
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Pokemon not found")
    @GetMapping("/{name}/name")
    public SoapProxyResponses.NameResponse getName(
            @Parameter(description = "Pokemon name", example = "pikachu", required = true)
            @PathVariable String name) {

        GetPokemonNameResponse soapResponse = pokemonService.getPokemonName(name);
        return new SoapProxyResponses.NameResponse(soapResponse.getName());
    }

    @Operation(
            summary = "Get Pokemon location encounters (SOAP proxy)",
            description = """
                    Retrieves the URL to Pokemon location area encounters.
                    
                    **Underlying SOAP Operation:** `GetPokemonLocationAreaEncountersRequest`
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Location encounters URL retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            value = "{\"locationAreaEncounters\": \"https://pokeapi.co/api/v2/pokemon/25/encounters\"}"
                    )
            )
    )
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Pokemon not found")
    @GetMapping("/{name}/locations")
    public SoapProxyResponses.LocationEncountersResponse getLocationEncounters(
            @Parameter(description = "Pokemon name", example = "pikachu", required = true)
            @PathVariable String name) {

        GetPokemonLocationAreaEncountersResponse soapResponse = pokemonService.getPokemonLocationAreaEncounters(name);
        return new SoapProxyResponses.LocationEncountersResponse(soapResponse.getLocationAreaEncounters());
    }
}
