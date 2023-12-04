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

  @ReadTestInput
  Scenario Outline: Check PBAC Permissions
    Given the API is available
    And the schema is written
    And the relationships are written
    When a user checks PBAC "<permissionName>" permission of "<subjRefObjId>" with principal "1234"
    Then the response code should be <responseCode>

    Examples:
      |subjRefObjId     | permissionName | responseCode |
      |customerB        | editContact    | 403          |