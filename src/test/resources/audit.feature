Feature: Controller Aspect Logging

  Scenario: Log before executing RoleController method
    Given Api and Kafka are available
    When a user edits a role with principal "pulse8admin"
    Then the role log should contain a message with the role topic

  Scenario: Log before executing AttributeController method
    Given Api and Kafka are available
    When a user gets attribute from policy with name "policytest"
    Then the attribute log should contain a message with the attribute topic

  Scenario Outline: Log before executing RelationshipController method
    Given Api and Kafka are available
    And the "<relation>" relationships are written
    When a user reads "<relation>" relationships with principal "1234"
    And the attribute log should contain a message with the relationship topic

    Examples:
      |relation     |
      |customerC    |

  Scenario: Log before executing PermissionsController method
    Given Api and Kafka are available
    When a user checks permissions with principal "1234"
    Then the permission log should contain a message with the permission topic

  Scenario: Log before executing PolicyController method
    Given Api and Kafka are available
    When a user gets all policy definitions
    Then the policy log should contain a message with the policy topic

  Scenario: Log before executing SchemaController method
    Given Api and Kafka are available
    When a user gets the schema
    Then the schema log should contain a message with the schema topic