Feature: Permissions API

  Scenario: Principal header is not set
    Given the API is available
    When a user checks permissions with principal ""
    Then the response code should be 403

  Scenario: Check Permissions
    Given the API is available
    When a user checks permissions with principal "1234"
    Then the response code should be 500
    And the response should contain "object definition `blog/post` not found"


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

  Scenario: Save Policy Definition
    Given the API is available
    When a user saves a policy definition with valid data
    Then the response code should be 200
    And the response should contain the policy ID

  Scenario: Get All Policy Definitions
    Given the API is available
    When a user gets all policy definitions
    Then the response code should be 200
    And the response should contain a list of policy definitions

  Scenario: Get Attributes from Existing Policy
    Given the API is available
    When a user gets attribute from policy with name "policytest"
    Then the response code should be 200
    And the response body should contain a map of policy attributes

  Scenario: Add Attributes to Existing Policy
    Given the API is available
    When a user adds attribute to an existing policy
    Then the attribute response code should be 200