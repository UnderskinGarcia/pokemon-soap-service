package com.bankaya.pokemon.infrastructure.adapter.persistence.entity;

import com.bankaya.pokemon.infrastructure.adapter.enums.RequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.With;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA Entity for Request Log
 * Represents the database table for storing request logs
 */
@Entity
@Table(name = "soap_audit_log")
@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SoapAuditLogEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_origin", nullable = false)
    private String ipOrigin;

    @Column(name = "soap_method", nullable = false)
    private String soapMethod;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "endpoint_class", length = 100)
    private String endpointClass;
}
