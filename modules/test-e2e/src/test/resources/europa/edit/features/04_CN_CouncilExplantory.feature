#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Council Explanatory Page in CN instance of Edit Application
@CouncilExplanatoryActRegressionScenariosEditCouncil
Feature: Council Explanatory Page Regression Features in Edit Council

  Background:
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then  navigate to Repository Browser page

  @basicCouncilExplanatoryFunctionalities
  Scenario: LEOS-4936 check default council explanatory funtionalities
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
    And  "Council Explanatory" section is displayed
    And  there are below columns displayed under council explanatory section
      | TITLE           |
      | LANGUAGE        |
      | LAST UPDATED ON |
      | LAST UPDATED BY |
    And  there are two default explanatories present in council explantory
    And  title of both default explanatories are
      | Coreper/Council note     |
      | Working Party cover page |
    And  delete button is disabled for both default explanatories
    And  add new explanatory button is displayed and enabled
    When click on add new explanatory button
    Then new council explanatory is added to council explanatory section
    And  title of council explanatory 1 is ""
    And  placeholder value of council explanatory 1 is "Add a title to this explanatory"
    When click on title input element of council explanatory 1
    Then save button is displayed in title input element of council explanatory 1
    And  cancel button is displayed in title input element of council explanatory 1
    When Add title "COUNCIL EXPLANATORY 1" to council explanatory 1
    And  click on save button for council explanatory 1
    Then "Explanatory metadata updated" message is displayed
    And  title of council explanatory 1 is "COUNCIL EXPLANATORY 1"
    And  delete button is enabled for council explanatory 1
    When click on open button of "COUNCIL EXPLANATORY 1" explanatory
    Then "COUNCIL EXPLANATORY 1" council explanatory page is displayed
    When click on close button in Council Explanatory page
    Then Proposal Viewer screen is displayed
    When click on open button of "Coreper/Council note" explanatory
    Then "Coreper/Council note" council explanatory page is displayed
    When click on close button in Council Explanatory page
    Then Proposal Viewer screen is displayed
    When click on open button of "Working Party cover page" explanatory
    Then "Working Party cover page" council explanatory page is displayed
    When click on close button in Council Explanatory page
    Then Proposal Viewer screen is displayed
    When click on delete button of council explanatory 1
    Then "Explanatory deletion : confirmation" pop up should be displayed with cancel and delete button enabled
    And  messages "Are sure you want to delete" and " the selected Explanatory and all its versions ?" are displayed in explanatory deletion : confirmation pop up window
    When click on delete button in Explanatory deletion : confirmation pop up
    Then non default explanatory is removed from council explanatory
    And  close the browser
