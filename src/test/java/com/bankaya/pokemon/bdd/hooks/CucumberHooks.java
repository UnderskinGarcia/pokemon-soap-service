package com.bankaya.pokemon.bdd.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.bankaya.pokemon.bdd.context.ScenarioContext;

import io.cucumber.java.Before;
import io.cucumber.java.After;

/**
 * Cucumber hooks for test lifecycle management
 */
public class CucumberHooks {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Autowired
    private ScenarioContext scenarioContext;

    @Before
    public void beforeScenario() {
        System.out.println("Starting Cucumber scenario");
        scenarioContext.reset();
        if (applicationContext != null) {
            System.out.println("Application context is available");
        }
    }

    @After
    public void afterScenario() {
        System.out.println("Ending Cucumber scenario");
        scenarioContext.reset();
    }
}