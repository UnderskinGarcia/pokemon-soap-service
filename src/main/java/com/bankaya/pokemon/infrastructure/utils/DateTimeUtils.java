package com.bankaya.pokemon.infrastructure.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Utilidad para manejo centralizado de fechas con zona horaria configurada
 * Utiliza la zona horaria definida en spring.jackson.time-zone
 */
@Getter
@Component
@Log4j2
public class DateTimeUtils {
    private final ZoneId applicationTimeZone;

    public DateTimeUtils(@Value("${spring.jackson.time-zone}") String timeZone) {
        this.applicationTimeZone = ZoneId.of(timeZone);
        log.info("🕐 DateTimeUtils inicializado con zona horaria: {}", timeZone);
    }

    /**
     * Función estática para obtener tiempo actual con fallback seguro.
     * Intenta usar DateTimeUtils si está disponible en el contexto de Spring,
     * de lo contrario usa zona horaria de México como fallback.
     * <p>
     *
     * @return LocalDateTime en la zona horaria configurada o America/Mexico_City como fallback
     */
    public static LocalDateTime getCurrentTimeWithFallback() {
        try {
            if (SpringContextUtils.isContextAvailable()) {
                DateTimeUtils dateTimeUtils = SpringContextUtils.getBean(DateTimeUtils.class);
                return dateTimeUtils.getCurrentTime();
            }
        } catch (Exception e) {
            log.error("getCurrentTimeWithFallback error: {}", e.getMessage());
        }

        // Fallback seguro usando zona horaria de México
        return LocalDateTime.now(ZoneId.of("America/Mexico_City"));
    }

    /**
     * Función de utilidad para obtener tiempo actual que puede ser usado desde AuditEntity
     * Mantiene consistencia con la zona horaria configurada de la aplicación
     *
     * @return LocalDateTime en la zona horaria configurada
     */
    public LocalDateTime getCurrentTime() {
        return now();
    }

    /**
     * Obtiene la fecha y hora actual en la zona horaria configurada de la aplicación
     *
     * @return LocalDateTime en la zona horaria configurada
     */
    public LocalDateTime now() {
        return LocalDateTime.now(applicationTimeZone);
    }

}
