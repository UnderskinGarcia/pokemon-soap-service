package com.bankaya.pokemon.domain.exception;

import javax.xml.namespace.QName;

import org.springframework.context.annotation.Configuration;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;


import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DetailedSoapFaultDefinitionExceptionResolver extends SoapFaultMappingExceptionResolver {

    private static final QName CODE = new QName("code");
    private static final QName MESSAGE = new QName("message");

    @Override
    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
        log.warn("Exception processed: {}", ex.getMessage());

        switch (ex) {
            case PokemonNotFoundException pnf -> {
                // SERVER fault for not found
                fault.setFaultActorOrRole("Pokemon not found: " + pnf.getMessage());

                SoapFaultDetail detail = fault.addFaultDetail();
                detail.addFaultDetailElement(CODE).addText("POKEMON_NOT_FOUND");
                detail.addFaultDetailElement(MESSAGE).addText(ex.getMessage());

            }
            case BadRequestException badRequestException -> {
                // CLIENT fault for invalid requests
                fault.setFaultActorOrRole("Invalid request: " + ex.getMessage());

                SoapFaultDetail detail = fault.addFaultDetail();
                detail.addFaultDetailElement(CODE).addText("INVALID_REQUEST");
                detail.addFaultDetailElement(MESSAGE).addText(ex.getMessage());

            }
            case IllegalArgumentException illegalArgumentException -> {
                // CLIENT fault for bad arguments
                fault.setFaultActorOrRole("Bad request: " + ex.getMessage());

                SoapFaultDetail detail = fault.addFaultDetail();
                detail.addFaultDetailElement(CODE).addText("BAD_REQUEST");
                detail.addFaultDetailElement(MESSAGE).addText(ex.getMessage());

            }
            default -> {
                // SERVER fault for unexpected errors
                fault.setFaultActorOrRole("Internal server error");

                SoapFaultDetail detail = fault.addFaultDetail();
                detail.addFaultDetailElement(CODE).addText("INTERNAL_ERROR");
                detail.addFaultDetailElement(MESSAGE).addText("An unexpected error occurred");
            }
        }

        // No necesitas setFaultCode aquí porque ya está configurado por el mapeo
        log.info("SOAP Fault customized for exception: {}", ex.getClass().getSimpleName());
    }
}