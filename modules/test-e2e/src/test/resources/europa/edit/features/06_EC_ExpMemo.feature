#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Explanatory Memorendum Page in EC instance of Edit Application
@ExplanatoryMemorendumPageRegressionScenariosEditCommission
Feature: Explanatory Memorandum Page Regression Features in Edit Commission

  Background:
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then  navigate to Repository Browser page

  @basicExpMemoScenario
  Scenario: LEOS-4146 [EC] Edition of elements - Browse Exp Memo Part
    When click on create proposal button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    When select template "SJ-023 - Proposal for a Regulation of the European Parliament and of the Council"
    Then next button is enabled
    When click on next button
    Then "Create new legislative document - Document metadata (2/2)" is displayed
    When provide document title "Automation Exp Memo Testing" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    When click on open button for explanatory memorandum
    Then explanatory memorandum page is displayed
    And  navigation pane is displayed
    And  toc editing button is not displayed
    And  explanatory memorandum content is displayed
    When click on "1. - CONTEXT OF THE PROPOSAL" present in the navigation pane
    Then page is redirected to "CONTEXT OF THE PROPOSAL"
    When select "CONTEXT OF THE PROPOSAL" in the page
    Then comment button is displayed
    Then highlight button is displayed
    Then suggest button is displayed
    When click on close button in explanatory memorandum page
    Then Proposal Viewer screen is displayed
    And  close the browser