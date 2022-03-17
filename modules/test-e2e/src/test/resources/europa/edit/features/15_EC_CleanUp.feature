#Author: Satyabrata Das
#Keywords Summary : Cleaning up Features
@CleanUpScenarios
Feature: Clean Up Features

  @cleanUpProposalCommission
  Scenario Outline: LEOS-4904 [EC] Delete all proposal created by Automation
    Given navigate to "Commission" edit application
    When  enter username "<username>" and password "<password>"
    Then  navigate to Repository Browser page
    And   delete all the proposal containing keyword
      | Regression |
      | Automation |
    And   close the browser
    Examples:
      | username               | password              |
      | user.support.1.name    | user.support.1.pwd    |
      | user.nonsupport.1.name | user.nonsupport.1.pwd |