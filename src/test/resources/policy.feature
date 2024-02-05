Feature: Role API

  Scenario: Edit Policy
    Given the API is available
    When a user edits a policy with correct input
    Then the response should be HTTP 200 and no errors
    When a user edits a policy with a non-existent policy name
    And the response should be HTTP 400 and the error message should contain "Policy not found"
    When a user edits a policy with a updated name that already exist
    And the response should be HTTP 400 and the error message should contain "Policy with the same name already exists"
    When a user edits a policy with a non-existent role
    And the response should be HTTP 400 and the error message should contain "Role not found"
