Feature: Basic Cucumber Test

  Scenario: Verify MockMvc is available
    Given the REST API is available
    When I call GET "/actuator/health"
    Then the response status should be 200
