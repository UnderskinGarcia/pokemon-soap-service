# Cucumber BDD Tests

Este proyecto incluye pruebas BDD escritas en Cucumber que replican la misma funcionalidad que los tests JUnit existentes.

## Estructura

### Archivos de Features (Gherkin)
- `src/test/resources/features/pokemon-endpoint.feature` - Pruebas del endpoint SOAP Pokemon
- `src/test/resources/features/soap-proxy-controller.feature` - Pruebas del controlador REST proxy SOAP
- `src/test/resources/features/pokemon-rest-controller.feature` - Pruebas del controlador REST Pokemon

### Step Definitions
- `src/test/java/com/bankaya/pokemon/bdd/steps/PokemonEndpointSteps.java` - Pasos para pruebas SOAP
- `src/test/java/com/bankaya/pokemon/bdd/steps/SoapProxyControllerSteps.java` - Pasos para pruebas proxy
- `src/test/java/com/bankaya/pokemon/bdd/steps/PokemonRestControllerSteps.java` - Pasos para pruebas REST

### Configuración
- `src/test/java/com/bankaya/pokemon/bdd/CucumberSpringConfiguration.java` - Configuración de Spring para Cucumber
- `src/test/java/com/bankaya/pokemon/bdd/hooks/CucumberHooks.java` - Hooks para ciclo de vida de tests
- `src/test/resources/cucumber.properties` - Propiedades de Cucumber

## Ejecutar los Tests

### JUnit Tests (Sin cambios)
```bash
./gradlew test
```

### Solo Cucumber (Future - requires custom test runner)
Actualmente, los tests de Cucumber se integran con el test runner de JUnit.

## Características

✅ Las pruebas JUnit originales permanecen sin cambios
✅ Nuevas pruebas BDD en Cucumber que duplican la cobertura
✅ Lenguaje Gherkin legible para stakeholders
✅ Step definitions reutilizables

## Cobertura de Pruebas

### Pokemon SOAP Endpoint Tests
- ✅ Solicitudes exitosas para diferentes endpoints
- ✅ Manejo de errores de solicitud inválida (CLIENT SOAP fault)
- ✅ Manejo de errores de no encontrado (SERVER SOAP fault)
- ✅ Verificación de WSDL accesible
- ✅ Nombres de Pokemon case-insensitive
- ✅ Múltiples habilidades

### SOAP Proxy Controller Tests
- ✅ Obtener abilities a través de REST
- ✅ Obtener base experience
- ✅ Obtener held items
- ✅ Obtener ID
- ✅ Obtener nombre
- ✅ Obtener location encounters
- ✅ Errores 404 para Pokemon inexistente
- ✅ Errores 400 para nombre vacío
- ✅ Diferentes Pokemon
- ✅ Case-insensitive names

### Pokemon REST Controller Tests
- ✅ Obtener Pokemon por nombre válido
- ✅ Errores para nombres inválidos
- ✅ Case-insensitive names
- ✅ Pokemon con abilities
- ✅ Pokemon con held items
- ✅ Pokemon con location encounters
- ✅ Manejo de nombres vacíos
- ✅ BadRequestException

## Reporte de Pruebas

Los reportes se generan en:
- `target/cucumber-reports/index.html` - Reporte HTML de Cucumber
- `build/reports/tests/test/index.html` - Reporte estándar de JUnit
