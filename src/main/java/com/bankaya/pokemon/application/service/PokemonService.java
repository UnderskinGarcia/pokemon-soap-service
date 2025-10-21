package com.bankaya.pokemon.application.service;

import org.springframework.context.ApplicationContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.bankaya.pokemon.domain.model.Pokemon;
import com.bankaya.pokemon.domain.ports.GetPokemonUseCase;
import com.bankaya.pokemon.domain.ports.PokemonApiPort;
import com.bankaya.pokemon.soap.Ability;
import com.bankaya.pokemon.soap.GetPokemonAbilitiesResponse;
import com.bankaya.pokemon.soap.GetPokemonBaseExperienceResponse;
import com.bankaya.pokemon.soap.GetPokemonHeldItemsResponse;
import com.bankaya.pokemon.soap.GetPokemonIdResponse;
import com.bankaya.pokemon.soap.GetPokemonLocationAreaEncountersResponse;
import com.bankaya.pokemon.soap.GetPokemonNameResponse;
import com.bankaya.pokemon.soap.HeldItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Application Service - Pokemon Use Case Implementation
 * This service implements the business logic for Pokemon operations
 * Part of the Application layer in Hexagonal Architecture
 * Caching Strategy:
 * - Cache by Pokemon name (pokemonByName)
 * - Cache by Pokemon ID (pokemonById)
 * - TTL and eviction policies configured in CacheConfig
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class PokemonService implements GetPokemonUseCase {

    private final PokemonApiPort pokemonApiPort;
    private final ApplicationContext applicationContext;

    private GetPokemonUseCase getSelf(){
        return applicationContext.getBean(GetPokemonUseCase.class);
    }

    @Override
    @Cacheable(value = "pokemonByName", key = "#pokemonName.toLowerCase()", unless = "#result == null")
    public Pokemon getPokemonByName(String pokemonName) {
        log.info("Fetching Pokemon by name from API (cache miss): {}", pokemonName);
        return pokemonApiPort.fetchPokemonByName(pokemonName);
    }

    @Override
    public GetPokemonAbilitiesResponse getPokemonAbilities(String pokemonName) {
        Pokemon pokemon = getSelf().getPokemonByName(pokemonName);

        GetPokemonAbilitiesResponse response = new GetPokemonAbilitiesResponse();

        if (pokemon.abilities() != null) {
            pokemon.abilities().forEach(ability -> {
                Ability soapAbility = new Ability();
                soapAbility.setName(ability.name());
                soapAbility.setUrl(ability.url());
                soapAbility.setIsHidden(ability.isHidden());
                soapAbility.setSlot(ability.slot());
                response.getAbilities().add(soapAbility);
            });
        }
        return response;
    }

    @Override public GetPokemonBaseExperienceResponse getPokemonBaseExperienceResponse(String pokemonName) {
        Pokemon pokemon = getSelf().getPokemonByName(pokemonName);

        GetPokemonBaseExperienceResponse response = new GetPokemonBaseExperienceResponse();
        response.setBaseExperience(pokemon.baseExperience());
        return response;
    }

    @Override public GetPokemonHeldItemsResponse getPokemonHeldItems(String pokemonName) {
        log.info("SOAP Request - Get Pokemon Held Items: {}", pokemonName);

        Pokemon pokemon = getSelf().getPokemonByName(pokemonName);

        GetPokemonHeldItemsResponse response = new GetPokemonHeldItemsResponse();

        if (pokemon.heldItems() != null) {
            pokemon.heldItems().forEach(item -> {
                HeldItem soapItem = new HeldItem();
                soapItem.setName(item.name());
                soapItem.setUrl(item.url());
                response.getHeldItems().add(soapItem);
            });
        }

        return response;
    }

    @Override
    public GetPokemonIdResponse getPokemonId(String pokemonName) {
        log.info("SOAP Request - Get Pokemon ID: {}", pokemonName);

        Pokemon pokemon = getSelf().getPokemonByName(pokemonName);

        GetPokemonIdResponse response = new GetPokemonIdResponse();
        response.setId(pokemon.id());

        return response;
    }

    @Override
    public GetPokemonNameResponse getPokemonName(String pokemonName) {
        log.info("SOAP Request - Get Pokemon Name: {}", pokemonName);

        Pokemon pokemon = getSelf().getPokemonByName(pokemonName);

        GetPokemonNameResponse response = new GetPokemonNameResponse();
        response.setName(pokemon.name());

        return response;
    }

    @Override
    public GetPokemonLocationAreaEncountersResponse getPokemonLocationAreaEncounters(String pokemonName) {
        log.info("SOAP Request - Get Pokemon Location Area Encounters: {}", pokemonName);

        Pokemon pokemon = getSelf().getPokemonByName(pokemonName);

        GetPokemonLocationAreaEncountersResponse response = new GetPokemonLocationAreaEncountersResponse();
        response.setLocationAreaEncounters(pokemon.locationAreaEncounters());

        return response;
    }

}
