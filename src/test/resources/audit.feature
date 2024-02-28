Feature: RoleController Aspect Logging

  Scenario: Log before executing RoleController method
    Given Api and Kafka are available
    When a user edits a role with principal "pulse8admin"
    Then the log should contain a message with the topic "logs-attributes"