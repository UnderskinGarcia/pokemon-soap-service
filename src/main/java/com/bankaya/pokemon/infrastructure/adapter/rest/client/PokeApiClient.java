package com.bankaya.pokemon.infrastructure.adapter.rest.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.bankaya.pokemon.domain.exception.PokemonNotFoundException;
import com.bankaya.pokemon.domain.exception.PokemonServiceException;
import com.bankaya.pokemon.domain.model.Pokemon;
import com.bankaya.pokemon.domain.ports.PokemonApiPort;
import com.bankaya.pokemon.infrastructure.adapter.rest.dto.PokemonApiResponse;
import com.bankaya.pokemon.infrastructure.adapter.rest.mapper.PokemonMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import reactor.core.publisher.Mono;

/**
 * PokeAPI Client Adapter (Output Adapter)
 * Implements the PokemonApiPort using WebClient to consume PokeAPI
 * This is part of the Infrastructure layer in Hexagonal Architecture
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class PokeApiClient implements PokemonApiPort {

    private final WebClient webClient;

    @Value("${pokeapi.base-url}")
    private String pokeApiBaseUrl;

    @Override
    public Pokemon fetchPokemonByName(String pokemonName) {
        log.info("Fetching Pokemon from PokeAPI: {}", pokemonName);

        try {
            PokemonApiResponse response = webClient
                    .get()
                    .uri(pokeApiBaseUrl + "/pokemon/{name}", pokemonName.toLowerCase())
                    .retrieve()
                    .onStatus(HttpStatus.NOT_FOUND::equals,
                            clientResponse -> Mono.error(new PokemonNotFoundException(pokemonName)))
                    .bodyToMono(PokemonApiResponse.class)
                    .block();

            if (response == null) {
                throw new PokemonServiceException("Empty response from PokeAPI");
            }

            log.info("Successfully fetched Pokemon: {}", response.name());
            return PokemonMapper.INSTANCE.toDomain(response);

        } catch (PokemonNotFoundException e) {
            log.error("Pokemon not found: {}", pokemonName);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching Pokemon from PokeAPI: {}", e.getMessage(), e);
            throw new PokemonServiceException("Error fetching Pokemon from PokeAPI", e);
        }
    }
}
