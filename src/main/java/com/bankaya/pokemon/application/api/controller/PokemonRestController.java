package com.bankaya.pokemon.application.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankaya.pokemon.domain.model.Pokemon;
import com.bankaya.pokemon.domain.ports.PokemonApiPort;

import lombok.RequiredArgsConstructor;

/**
 * Pokémon TEST REST Controller to validate consumed external API
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pokemon")
public class PokemonRestController {

    private final PokemonApiPort pokemonApiPort;

    @GetMapping("/{pokemonName}")
    public Pokemon getPokemonByName(@PathVariable String pokemonName) {
        return pokemonApiPort.fetchPokemonByName(pokemonName);
    }

}
