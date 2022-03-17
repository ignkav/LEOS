#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Explanatory Memorendum Page in CN instance of Edit Application
@ExplanatoryMemorendumPageRegressionScenariosEditCouncil
Feature: Explanatory Memorendum Page Regression Features in Edit Council

  Background:
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then  navigate to Repository Browser page

  @basicExpMemoScenario
  Scenario: LEOS-4146 CN Edition of elements - Browse Exp Memo Part
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "upload/PROP_ACT_3530794399585608473.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on "Next" button
    Then upload screen is showing with "Create new mandate - Document metadata (2/2)" page
    When click on "Create" button
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
    When click on annotation pop up button
    When select "CONTEXT OF THE PROPOSAL" in the page
    Then comment button is displayed
    Then highlight button is displayed
    When click on close button in explanatory memorandum page
    Then Proposal Viewer screen is displayed
    And  close the browser