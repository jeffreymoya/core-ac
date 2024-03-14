Feature: Controller Aspect Logging

  Scenario: Log before executing RoleController method
    Given Api and Kafka are available
    When a user edits a role with principal "pulse8admin"
    Then the role log should contain a message with the role topic

  Scenario: Log before executing AttributeController method
    Given Api and Kafka are available
    When a user gets attribute from policy with name "policytest"
    Then the attribute log should contain a message with the attribute topic
