package com.bankaya.pokemon.domain.model;

import java.time.LocalDateTime;

import com.bankaya.pokemon.infrastructure.adapter.enums.RequestStatus;

import lombok.Builder;
import lombok.Data;
import lombok.With;

/**
 * RequestLog Domain Entity
 * Represents the audit log for each request made to the service
 */
@Data
@Builder
@With
public class SoapAuditLog {
    private Long id;
    private String ipOrigin;
    private LocalDateTime requestDate;
    private String soapMethod;
    private Long startTime;
    private Long durationMs;
    private String requestPayload;
    private String responsePayload;
    private RequestStatus status;
    private String errorMessage;
    private String endpointClass;
}
