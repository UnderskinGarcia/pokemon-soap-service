package com.bankaya.pokemon.domain.ports;

import com.bankaya.pokemon.infrastructure.adapter.persistence.entity.SoapAuditLogEntity;

/**
 * Output Port - Repository Interface
 * Defines the contract for persisting request logs
 */
public interface SoapAuditLogRepositoryPort {
    /**
     * Save a request log
     * @param audit the request log to save
     */
    void save(SoapAuditLogEntity audit);
}
