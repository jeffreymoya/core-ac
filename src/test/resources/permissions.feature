Feature: Permissions API

  Scenario: Check Permissions
    Given the API is available
    When a user checks permissions
    Then the response code should be 500
    And the response should contain "FAILED_PRECONDITION: object definition `blog/post` not found"
