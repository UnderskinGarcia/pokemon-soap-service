package com.bankaya.pokemon.infrastructure.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utilidad para acceder al contexto de Spring desde clases que no son beans administrados
 * Especialmente útil para entidades JPA que necesitan acceso a servicios de Spring
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    private static final AtomicReference<ApplicationContext> context = new AtomicReference<>();

    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        context.set(applicationContext);
    }

    /**
     * Obtiene un bean del contexto de Spring por su tipo
     * @param beanClass Clase del bean a obtener
     * @param <T> Tipo del bean
     * @return Instancia del bean
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.get().getBean(beanClass);
    }

    /**
     * Verifica si el contexto está disponible
     * @return true si el contexto está disponible
     */
    public static boolean isContextAvailable() {
        return context.get() != null;
    }
}