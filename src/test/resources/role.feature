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

  Scenario Outline: Assign Role to User
    Given the API is available
    And the "adviser" role is written in the resource "<resource>"
    When a user writes "<roleName>" relationship to the resource with principal "1234"
    Then the write relationship response code should be 200
    And the user "<userId>" should have "update" permission to the related resource

    Examples:
      |roleName                | resource      | userId        |
      |adviser                 | userprofile   | adviserA      |
      |system_administrator    | userprofile   | systemAdmin1  |
