package com.bankaya.pokemon.bdd.context;

import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

import lombok.Getter;
import lombok.Setter;

/**
 * Shared context for storing test state across step definitions
 */
@Setter
@Getter
@Component
public class ScenarioContext {

    private MvcResult lastResponse;

    public void reset() {
        this.lastResponse = null;
    }
}