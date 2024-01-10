Feature: Role API

  Scenario: Edit Role
    Given the API is available
    When a user edits a role with principal "1234"
    Then the response should be HTTP 200
    And the response should contain the edited role in the schema
