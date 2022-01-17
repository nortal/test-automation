Feature: Test with execution groups feature

  @skip
  Scenario: A skippable failing scenario
    Given A step is called
    When Something is called
    Then A failing step

  @execution-group-a-b
  Scenario: Sample scenario 1
    Given A step is called
    When Something is called
    Then Something is done

  @execution-group-a-b
  Scenario: Sample scenario 2
    Given A step is called
    When Something is called
    Then Something is done

  @execution-group-a
  Scenario: Sample scenario 3
    Given A step is called
    When Something is called
    Then Something is done

  @execution-group-a
  Scenario: Sample scenario 4
    Given A step is called
    When Something is called
    Then Something is done

  Scenario: Sample scenario 5
    Given A step is called
    When Something is called
    Then Something is done

  @execution-group-c
  Scenario: Sample scenario 6
    Given A step is called
    When Something is called
    Then Something is done

  Scenario: Sample scenario 7
    Given A step is called
    When Something is called
    Then Something is done