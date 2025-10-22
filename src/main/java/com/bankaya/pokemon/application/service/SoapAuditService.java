package com.bankaya.pokemon.application.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankaya.pokemon.domain.model.SoapAuditLog;
import com.bankaya.pokemon.domain.ports.SoapAuditLogRepositoryPort;
import com.bankaya.pokemon.infrastructure.adapter.persistence.entity.SoapAuditLogEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class SoapAuditService {

    private final SoapAuditLogRepositoryPort repository;

    @Async
    @Transactional
    public void saveAuditLog(SoapAuditLog soapAuditLog) {
        try {
            SoapAuditLogEntity soapAuditLogEntity = SoapAuditLogEntity
                    .builder().build()
                    .withIpOrigin(soapAuditLog.getIpOrigin())
                    .withSoapMethod(soapAuditLog.getSoapMethod())
                    .withDurationMs(soapAuditLog.getDurationMs())
                    .withRequestPayload(formatXml(soapAuditLog.getRequestPayload()))
                    .withResponsePayload(formatXml(soapAuditLog.getResponsePayload()))
                    .withStatus(soapAuditLog.getStatus())
                    .withEndpointClass(soapAuditLog.getEndpointClass())
                    .withErrorMessage(soapAuditLog.getErrorMessage());

            repository.save(soapAuditLogEntity);

        } catch (Exception e) {
            log.error("Error saving audit log to database", e);
        }
    }

    private String formatXml(String xml) {
        if (xml == null) {
            return null;
        }

        try {
            // Opcional: formatear/minificar XML para ahorrar espacio
            return xml.replaceAll(">\\s+<", "><").trim();
        } catch (Exception e) {
            return xml;
        }
    }
}