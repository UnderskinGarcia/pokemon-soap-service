package com.bankaya.pokemon.infrastructure.adapter.persistence;

import org.springframework.stereotype.Component;

import com.bankaya.pokemon.domain.ports.SoapAuditLogRepositoryPort;
import com.bankaya.pokemon.infrastructure.adapter.persistence.entity.SoapAuditLogEntity;
import com.bankaya.pokemon.infrastructure.adapter.persistence.repository.JpaAuditLogRepository;

import lombok.RequiredArgsConstructor;

/**
 * Adapter for SOAP Audit Log Repository
 * Implements the port interface and delegates to Spring Data JPA repository
 */
@Component
@RequiredArgsConstructor
public class SoapAuditLogRepositoryAdapter implements SoapAuditLogRepositoryPort {

    private final JpaAuditLogRepository jpaRepository;

    @Override
    public void save(SoapAuditLogEntity audit) {
        jpaRepository.save(audit);
    }
}