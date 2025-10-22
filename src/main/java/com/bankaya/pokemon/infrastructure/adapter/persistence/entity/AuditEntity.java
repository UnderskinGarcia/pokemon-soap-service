package com.bankaya.pokemon.infrastructure.adapter.persistence.entity;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.bankaya.pokemon.infrastructure.utils.DateTimeUtils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
@Log4j2
public class AuditEntity {
    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @PrePersist
    protected void onCreate() {
        if (requestDate == null) {
            requestDate = DateTimeUtils.getCurrentTimeWithFallback();
        }
    }
}