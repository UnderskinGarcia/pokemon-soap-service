package com.bankaya.pokemon.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Domain Exception - Bad Request
 * Thrown when the request parameters are invalid
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
@SoapFault(faultCode = FaultCode.CLIENT)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
