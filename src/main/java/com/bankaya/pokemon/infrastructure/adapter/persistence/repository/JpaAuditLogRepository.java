package com.bankaya.pokemon.infrastructure.adapter.persistence.repository;

import com.bankaya.pokemon.infrastructure.adapter.persistence.entity.SoapAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for Request Log
 * Spring Data JPA repository interface
 */
@Repository
public interface JpaAuditLogRepository extends JpaRepository<SoapAuditLogEntity, Long> {
}
