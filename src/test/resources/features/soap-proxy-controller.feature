Feature: SOAP Proxy Controller REST Endpoints
  As a REST API client
  I want to call REST endpoints that proxy SOAP operations
  So that I can retrieve Pokemon information through REST

  Background:
    Given the REST API is available
    And the SOAP service is mocked

  Scenario: Get Pokemon abilities through REST proxy
    Given I have mocked Pokemon "pikachu" with 2 abilities
    When I call GET "/api/soap/pokemon/pikachu/abilities"
    Then the response status should be 200
    And the response should contain 2 abilities
    And the first ability should be "static"
    And the response "abilities" should contain URL "https://pokeapi.co/api/v2/ability/9/" into array "abilities"

  Scenario: Get Pokemon base experience through REST proxy
    Given I have mocked Pokemon "pikachu" with base experience 112
    When I call GET "/api/soap/pokemon/pikachu/base-experience"
    Then the response status should be 200
    And the response should contain base_experience value 112

  Scenario: Get Pokemon held items through REST proxy
    Given I have mocked Pokemon "pikachu" with held item "light-ball"
    When I call GET "/api/soap/pokemon/pikachu/held-items"
    Then the response status should be 200
    And the response should contain 1 held item
    And the first held item should be "light-ball"
    And the response "held-items" should contain URL "https://pokeapi.co/api/v2/item/213/" into array "held_items"

  Scenario: Get Pokemon ID through REST proxy
    Given I have mocked Pokemon "pikachu" with ID 25
    When I call GET "/api/soap/pokemon/pikachu/id"
    Then the response status should be 200
    And the response should contain id value 25

  Scenario: Get Pokemon name through REST proxy
    Given I have mocked Pokemon "pikachu" with name "pikachu"
    When I call GET "/api/soap/pokemon/pikachu/name"
    Then the response status should be 200
    And the response should contain name value "pikachu"

  Scenario: Return 404 for non-existent Pokemon
    Given the Pokemon "nonexistentpokemon123456" does not exist
    When I call GET "/api/soap/pokemon/nonexistentpokemon123456/id"
    Then the response status should be 404

  Scenario: Return 404 for empty Pokemon name
    Given I have an empty Pokemon name
    When I call GET "/api/soap/pokemon//id"
    Then the response status should be 404

  Scenario Outline: Handle different Pokemon names correctly
    Given I have mocked Pokemon "<pokemonName>" with ID <expectedId>
    When I call GET "/api/soap/pokemon/<pokemonName>/id"
    Then the response status should be 200
    And the response should contain id value <expectedId>

    Examples:
      | pokemonName | expectedId |
      | bulbasaur   | 1          |
      | charmander  | 4          |
      | squirtle    | 7          |

  Scenario Outline: Handle case-insensitive Pokemon names
    Given I have mocked Pokemon "pikachu" with ID 25
    When I call GET "/api/soap/pokemon/<pokemonNameVariation>/id"
    Then the response status should be 200
    And the response should contain id value 25

    Examples:
      | pokemonNameVariation |
      | PIKACHU              |
      | Pikachu              |
      | pikachu              |
      | pIkAcHu              |
      | PIKACHU              |
