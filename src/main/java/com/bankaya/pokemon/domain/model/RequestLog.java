package com.bankaya.pokemon.domain.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.With;

/**
 * RequestLog Domain Entity
 * Represents the audit log for each request made to the service
 */
@Builder
@With
public record RequestLog(
        Long id,
        String ipOrigin,
        LocalDateTime requestDate,
        String methodExecuted,
        Long durationMs,
        String request,
        String response) {
}
