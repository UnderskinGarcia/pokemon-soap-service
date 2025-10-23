package com.bankaya.pokemon.application.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankaya.pokemon.domain.model.Pokemon;
import com.bankaya.pokemon.domain.ports.PokemonApiPort;

import lombok.RequiredArgsConstructor;

/**
 * Pokémon REST Controller
 * Provides REST API access to Pokemon data as an alternative to SOAP
 */
@Tag(name = "Pokemon REST API", description = "REST endpoints for accessing Pokemon data from PokeAPI, ONLY test purposes")
@RestController
@RequiredArgsConstructor
@RequestMapping("/pokemon")
public class PokemonRestController {

    private final PokemonApiPort pokemonApiPort;

    @Operation(
            summary = "Get Pokemon by name",
            description = """
                    Retrieves complete Pokemon information by name from PokeAPI.
                    The response includes abilities, base experience, held items, location encounters, and more.
                    Pokemon names are case-insensitive.
                    Results are cached for improved performance.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pokemon found successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Pokemon.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Pokemon name is empty or invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pokemon not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    @GetMapping("/{pokemonName}")
    public Pokemon getPokemonByName(
            @Parameter(
                    description = "Name of the Pokemon to retrieve (case-insensitive). Examples: pikachu, charizard, mewtwo",
                    required = true,
                    example = "pikachu"
            )
            @PathVariable String pokemonName) {
        return pokemonApiPort.fetchPokemonByName(pokemonName);
    }

}
