#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Proposal Browser Page in CN instance of Edit Application
@ProposalBrowserPageRegressionScenariosEditCouncil
Feature: Proposal Browser Page Regression Features in Edit Council

  Background:
    Given navigate to "Council" edit application

  @closeButtonNotVisibleforNonSupportUser
  Scenario: LEOS-5025 [CN] Close button is not available for non support user in proposal browser page
    When enter username "user.nonsupport.1.name" and password "user.nonsupport.1.pwd"
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    And  close button is not displayed
    And  close the browser

  @createMandate_CreateMileStone_mandateTitleChange_deleteMandate
  Scenario: LEOS-4584,4145 [CN] Verify User is able to do create mandate, create milestone, title change of mandate and delete mandate
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
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
    When click on download button
    And  explanatory memorandum section is present
    And  legal act section is present
    And  collaborators section is Present
    And  "DAS Satyabrata" is added as "Author" in collaborators section
    And  milestones section is present
    And  no milestone exists in milestones section
    When click on add button in milestones section
    Then Add a milestone window is displayed
    When click on milestone dropdown icon
    Then "Meeting of the Council" option is selected by default
    Then milestone title textbox is disabled
    Then these are below options displayed for milestone dropdown
      | Meeting of the Council |
      | Other                  |
    When click on milestone option as Other
    And  type "Council proposal" in title box
    When click on "Create milestone" button
    Then "Milestone creation has been requested" message is displayed
    Then Proposal Viewer screen is displayed
    And  "Council proposal" is showing in title column of milestones table
    And  today's date is showing in date column of milestones table
    And  "File ready" is showing in status column of milestones table
    When click on the link in title column of the first milestone
    Then milestone explorer page is displayed
    And  explanatory memorandum section is displayed
    And  legal act section is displayed
    And  click on close button present in milestone explorer page
    Then Proposal Viewer screen is displayed
    When click on title of the mandate
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " Title change" keyword in the title of the proposal/mandate
    And  click on title save button
    Then message "Metadata saved" is displayed
    Then title of the proposal/mandate contains "Title change" keyword
    When click on delete button
    Then mandate deletion confirmation page should be displayed
    And  message "Are you sure you want to delete the mandate and contained documents?" is displayed
    And  cancel button is displayed and enabled in proposal deletion confirmation pop up
    And  delete button is displayed and enabled in proposal deletion confirmation pop up
    When click on delete button present in confirmation pop up
    Then navigate to Repository Browser page
    And  close the browser