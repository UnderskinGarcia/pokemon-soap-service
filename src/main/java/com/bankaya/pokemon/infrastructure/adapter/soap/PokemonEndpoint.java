package com.bankaya.pokemon.infrastructure.adapter.soap;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.bankaya.pokemon.application.service.PokemonService;
import com.bankaya.pokemon.domain.exception.BadRequestException;
import com.bankaya.pokemon.soap.GetPokemonAbilitiesResponse;
import com.bankaya.pokemon.soap.GetPokemonBaseExperienceResponse;
import com.bankaya.pokemon.soap.GetPokemonHeldItemsResponse;
import com.bankaya.pokemon.soap.GetPokemonIdResponse;
import com.bankaya.pokemon.soap.GetPokemonLocationAreaEncountersResponse;
import com.bankaya.pokemon.soap.GetPokemonNameResponse;
import com.bankaya.pokemon.soap.PokemonNameRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * SOAP Endpoint for Pokemon Web Service
 * Input Adapter in Hexagonal Architecture
 */
@Log4j2
@Endpoint
@RequiredArgsConstructor
public class PokemonEndpoint {

    private static final String NAMESPACE_URI = "http://bankaya.com/pokemon/soap";

    private final PokemonService pokemonService;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetPokemonAbilitiesRequest")
    @ResponsePayload
    public GetPokemonAbilitiesResponse getPokemonAbilities(@RequestPayload PokemonNameRequest request) {
        log.info("SOAP Request - Get Pokemon Abilities: {}", request.getName());

        return pokemonService.getPokemonAbilities(request.getName());
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetPokemonBaseExperienceRequest")
    @ResponsePayload
    public GetPokemonBaseExperienceResponse getPokemonBaseExperience(
            @RequestPayload PokemonNameRequest request) {
        log.info("SOAP Request - Get Pokemon Base Experience: {}", request.getName());

        return pokemonService.getPokemonBaseExperienceResponse(request.getName());
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetPokemonHeldItemsRequest")
    @ResponsePayload
    public GetPokemonHeldItemsResponse getPokemonHeldItems(@RequestPayload PokemonNameRequest request) {
        log.info("SOAP Request - Get Pokemon Held Items: {}", request.getName());

        return pokemonService.getPokemonHeldItems(request.getName());
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetPokemonIdRequest")
    @ResponsePayload
    public GetPokemonIdResponse getPokemonId(@RequestPayload PokemonNameRequest request) {
        log.info("SOAP Request - Get Pokemon ID: {}", request.getName());

        return pokemonService.getPokemonId(request.getName());
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetPokemonNameRequest")
    @ResponsePayload
    public GetPokemonNameResponse getPokemonName(@RequestPayload PokemonNameRequest request) {
        log.info("SOAP Request - Get Pokemon Name: {}", request.getName());

        return pokemonService.getPokemonName(request.getName());
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetPokemonLocationAreaEncountersRequest")
    @ResponsePayload
    public GetPokemonLocationAreaEncountersResponse getPokemonLocationAreaEncounters(
            @RequestPayload PokemonNameRequest request) {
        log.info("SOAP Request - Get Pokemon Location Area Encounters: {}", request.getName());

        return pokemonService.getPokemonLocationAreaEncounters(request.getName());
    }
}