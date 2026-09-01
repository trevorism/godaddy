Feature: The trevorism.com dns records are administered through this API

  Scenario: Anonymous callers cannot read the zone
    Given the application is alive
    When the records are requested anonymously
    Then the request is rejected

  Scenario: Anonymous callers cannot write to the zone
    Given the application is alive
    When a record creation is attempted anonymously
    Then the request is rejected

  Scenario: A system caller can read the zone
    Given the application is alive
    When the records are requested as a system caller
    Then some records are returned

  Scenario: A system caller can add and then remove a txt record
    Given the application is alive
    When a txt record is created under a name unique to this run
    Then the txt record is present at that name
    And deleting the txt records at that name leaves none behind
