package com.bankaya.pokemon.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Domain Exception - Generic Pokemon Service Exception
 * Thrown when there's an error in the Pokemon service operations
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
@SoapFault(faultCode = FaultCode.SERVER)
public class PokemonServiceException extends RuntimeException {
    public PokemonServiceException(String message) {
        super(message);
    }

    public PokemonServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
