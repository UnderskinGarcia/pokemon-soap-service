package com.bankaya.pokemon.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Domain Exception - Pokemon Not Found
 * Thrown when a Pokemon is not found in the external API
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
@SoapFault(faultCode = FaultCode.SERVER)
public class PokemonNotFoundException extends RuntimeException {
    public PokemonNotFoundException(String pokemonName) {
        super(String.format("Pokemon with name '%s' not found", pokemonName));
    }
}
