package com.bankaya.pokemon.domain.ports;

import com.bankaya.pokemon.domain.model.RequestLog;

/**
 * Output Port - Repository Interface
 * Defines the contract for persisting request logs
 * This is part of the hexagonal architecture - driven side
 */
public interface RequestLogRepositoryPort {

    /**
     * Save a request log
     * @param requestLog the request log to save
     * @return the saved request log with generated ID
     */
    RequestLog save(RequestLog requestLog);
}
