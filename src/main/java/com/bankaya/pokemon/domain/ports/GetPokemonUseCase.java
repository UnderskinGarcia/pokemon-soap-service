package com.bankaya.pokemon.domain.ports;

import com.bankaya.pokemon.domain.model.Pokemon;

/**
 * Input Port - Use Case Interface
 * Defines the contract for retrieving Pokemon information
 * This is part of the hexagonal architecture - driving side
 */
public interface GetPokemonUseCase {

    /**
     * Get Pokemon by name
     * @param pokemonName the name of the Pokemon
     * @return Pokemon domain model
     */
    Pokemon getPokemonByName(String pokemonName);

}
