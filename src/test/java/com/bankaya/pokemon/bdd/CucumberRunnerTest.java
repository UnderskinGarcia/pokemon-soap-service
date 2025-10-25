package com.bankaya.pokemon.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * Test runner para ejecutar las pruebas BDD de Cucumber con Spring Boot
 *
 * La configuración de Spring se proporciona a través de la clase CucumberSpringConfiguration
 * que está anotada con @CucumberContextConfiguration
 *
 * Ejecutar con:
 * ./gradlew test --tests CucumberRunnerTest
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.bankaya.pokemon.bdd")
public class CucumberRunnerTest {
}
