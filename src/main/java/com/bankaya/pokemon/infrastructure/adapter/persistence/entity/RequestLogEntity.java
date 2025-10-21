package com.bankaya.pokemon.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity for Request Log
 * Represents the database table for storing request logs
 */
@Entity
@Table(name = "request_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_origin", nullable = false)
    private String ipOrigin;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "method_executed", nullable = false)
    private String methodExecuted;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "request", columnDefinition = "TEXT")
    private String request;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;
}
