Feature: Permissions API

  Scenario: Principal header is not set
    Given the API is available
    When a user checks permissions with principal ""
    Then the response code should be 403

  Scenario: Check Permissions
    Given the API is available
    When a user checks permissions with principal "1234"
    Then the response code should be 500
    And the response should contain "FAILED_PRECONDITION"


  Scenario Outline: Check PBAC Permissions
    Given the API is available
    When a user checks PBAC "<permissionName>" permission of "<subjRefObjId>" with principal "1234"
    Then the response code should be <responseCode>


    Examples:
      |subjRefObjId     | permissionName | responseCode |
      |customerB        | editContact    | 403          |


  Scenario: Lookup subjects
    Given the API is available
    When a user do a "subjects" lookup with principal "1234"
    Then the response code should be 200
    And the response body should contain the "subjects" data

  Scenario: Lookup resources
    Given the API is available
    When a user do a "resources" lookup with principal "1234"
    Then the response code should be 200
    And the response body should contain the "resources" data