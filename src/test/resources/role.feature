Feature: Role API

  Scenario: Edit Role
    Given the API is available
    When a user edits a role with principal "1234"
    Then the response should be HTTP 200
    And the response should contain the edited role in the schema

  Scenario: Delete role
    Given the API is available
    And the "adviser" role is written in the resource "userprofile" and has relationships
    When a user deletes "adviser" relationships by "filter" with principal "1234"
    Then the delete relationship response code should be 204
    When a user deletes "adviser" role under "userprofile" policy using principal "1234"
    Then the response code should be 204

  Scenario: View Roles of User
    Given the API is available
    When  user wants to view roles of user with subject reference id "customerA" and subject reference type "user" from object type "userprofile"
    Then the response code should be HTTP 200
    And the response should contain the list of roles and permissions associated to it