Feature: Pokemon SOAP Endpoint Integration Tests
  As a SOAP client
  I want to call the Pokemon SOAP endpoints
  So that I can retrieve Pokemon information in SOAP format

  Background:
    Given the SOAP endpoint is available
    And the namespace URI is "http://bankaya.com/pokemon/soap"

  Scenario: Successfully retrieve Pokemon ID via SOAP
    When I send a GetPokemonIdRequest for "pikachu"
    Then the response should contain no SOAP fault
    And the response should contain id with value "25"

  Scenario: Successfully retrieve Pokemon ID via SOAP
    When I send a GetPokemonIdRequest for "25"
    Then the response should contain no SOAP fault
    And the response should contain id with value "pikachu"

  Scenario: Successfully retrieve Pokemon name via SOAP
    When I send a GetPokemonNameRequest for "pikachu"
    Then the response should contain no SOAP fault
    And the response should contain name with value "pikachu"

  Scenario: Successfully retrieve Pokemon abilities via SOAP
    When I send a GetPokemonAbilitiesRequest for "pikachu"
    Then the response should contain no SOAP fault
    And the response should contain abilities element

  Scenario: Handle case-insensitive Pokemon names correctly
    When I send a GetPokemonIdRequest for "PIKACHU"
    Then the response should contain no SOAP fault
    And the response should contain id with value "25"

  Scenario: Verify WSDL is accessible
    When I request the WSDL at "/pokemon/ws/pokemon.wsdl"
    Then the response status code should be 200
    And the response should contain "definitions"
    And the response should contain "pokemon"