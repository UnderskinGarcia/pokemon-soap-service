package com.bankaya.pokemon.domain.exception;

/**
 * Domain Exception - Generic Pokemon Service Exception
 * Thrown when there's an error in the Pokemon service operations
 */
public class PokemonServiceException extends RuntimeException {
    public PokemonServiceException(String message) {
        super(message);
    }

    public PokemonServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
