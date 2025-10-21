package com.bankaya.pokemon.domain.ports;

import com.bankaya.pokemon.domain.model.Pokemon;
import com.bankaya.pokemon.soap.GetPokemonAbilitiesResponse;
import com.bankaya.pokemon.soap.GetPokemonBaseExperienceResponse;
import com.bankaya.pokemon.soap.GetPokemonHeldItemsResponse;
import com.bankaya.pokemon.soap.GetPokemonIdResponse;
import com.bankaya.pokemon.soap.GetPokemonLocationAreaEncountersResponse;
import com.bankaya.pokemon.soap.GetPokemonNameResponse;

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

    GetPokemonAbilitiesResponse getPokemonAbilities(String pokemonName);

    GetPokemonBaseExperienceResponse getPokemonBaseExperienceResponse(String pokemonName);

    GetPokemonHeldItemsResponse getPokemonHeldItems(String pokemonName);

    GetPokemonIdResponse getPokemonId(String pokemonName);

    GetPokemonNameResponse getPokemonName(String pokemonName);

    GetPokemonLocationAreaEncountersResponse getPokemonLocationAreaEncounters(String pokemonName);
}
