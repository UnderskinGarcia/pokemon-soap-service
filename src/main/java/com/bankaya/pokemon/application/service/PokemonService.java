package com.bankaya.pokemon.application.service;

import org.springframework.stereotype.Service;

import com.bankaya.pokemon.domain.model.Pokemon;
import com.bankaya.pokemon.domain.ports.GetPokemonUseCase;
import com.bankaya.pokemon.domain.ports.PokemonApiPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Application Service - Pokemon Use Case Implementation
 * This service implements the business logic for Pokemon operations
 * Part of the Application layer in Hexagonal Architecture
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class PokemonService implements GetPokemonUseCase {

    private final PokemonApiPort pokemonApiPort;

    @Override
    public Pokemon getPokemonByName(String pokemonName) {
        log.info("Fetching Pokemon by name: {}", pokemonName);
        return pokemonApiPort.fetchPokemonByName(pokemonName);
    }

}
