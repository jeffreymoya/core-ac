Feature: Role API

  Scenario: Delete Resource
    Given kafka is available
    When a delete resource message is sent with resourceId "admin" and resourceType "pulse8"
    Then the message should be consumed by the consumer