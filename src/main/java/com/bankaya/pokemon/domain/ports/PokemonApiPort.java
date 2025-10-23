package com.bankaya.pokemon.domain.ports;

import com.bankaya.pokemon.domain.model.Pokemon;

/**
 * Output Port - External API Interface
 * Defines the contract for retrieving Pokemon from external API (PokeAPI)
 * This is part of the hexagonal architecture - driven side
 */
public interface PokemonApiPort {

    /**
     * Fetch Pokemon data from external API
     * @param pokemonName the name of the Pokemon
     * @return Pokemon domain model
     */
    Pokemon fetchPokemonByName(String pokemonName);
}
