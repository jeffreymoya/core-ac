Feature: Permissions API

  Scenario: Principal header is not set
    Given the API is available
    When a user checks permissions with principal ""
    Then the response code should be 403

  Scenario: Check Permissions
    Given the API is available
    When a user checks permissions with principal "1234"
    Then the response code should be 500
    And the error should contain "object definition `blog/post` not found"


  Scenario Outline: Check PBAC Permissions
    Given the API is available
    When a user checks PBAC "<permissionName>" permission of "<subjRefObjId>" with principal "1234"
    Then the response code should be <responseCode>


    Examples:
      |subjRefObjId     | permissionName | responseCode |
      |customerA        | update         | 200          |
      |customerB        | update         | 403          |


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

  Scenario Outline: Read relationships
    Given the API is available
    And the "<relation>" relationships are written
    When a user reads "<relation>" relationships with principal "1234"
    Then the response code should be 200
    And the response body should contain the "<relation>" relationship list

    Examples:
      |relation     |
      |customerC    |

  Scenario Outline: Delete relationships
    Given the API is available
    And the "<relation>" relationships are written
    When a user deletes "<relation>" relationships by "<option>" with principal "1234"
    Then the delete response code should be 204
    When a user reads "<relation>" relationships with principal "1234"
    Then the response code should be 200
    And the response body should contain the list size 0

    Examples:
      |option     | relation  |
      |filter     | customerC |
      |path       | customerC |

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

  Scenario: Assign Policy to Resource
    Given the API is available
    When a user writes "adviser" relation and permission to the "userprofile" resource with principal "1234"
    Then the response code should be 200
    When a user writes "adviser" relationship to the resource with principal "1234"
    Then the response code should be 200
    When a user checks PBAC "update" permission of "adviserA" with principal "1234"
    Then the response code should be 200

  Scenario: Get Definition from Existing Policy
    Given the API is available
    When a user gets definition of policy with name "policytest"
    Then the response code should be 200
    And the response body should contain a map of policy definition

  Scenario: Get Attributes from Existing Policy
    Given the API is available
    When a user gets attribute from policy with name "policytest"
    Then the response code should be 200
    And the response body should contain a map of policy attributes

  Scenario: Add Attributes to Existing Policy
    Given the API is available
    When a user adds attribute to an existing policy
    Then the attribute response code should be 200

  Scenario: Check Route Permissions without resourceType
    Given the API is available
    When a user checks route permissions via "GET" with principal "1234" and route "/pulse8/123" and uriTemplate "/{/?}{resourceId:.*}"
    Then the response code should be 400
    And the response should contain "Object type is required when URI Template does not contain resourceType"

  Scenario: Check Route Permissions without resourceType
    Given the API is available
    When a user checks route permissions via "GET" with principal "1234" and route "/pulse8/123" and object type "nonexistingtype"
    Then the response code should be 500
    And the response should contain "object definition `nonexistingtype` not found"

  Scenario: Check Route Permissions
    Given the API is available
    When a user checks route permissions via "GET" with principal "1234" and route "/pulse8/123" and uriTemplate "/{resourceType}{/?}{resourceId:.*}"
    Then the response code should be 403
    And the response should contain "has_permission"