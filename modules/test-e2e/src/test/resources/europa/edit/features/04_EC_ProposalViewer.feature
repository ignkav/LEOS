#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Proposal Viewer Page
@ProposalViewerRegressionScenariosEditCommission
Feature: Proposal Viewer Regression Features in Edit Commission

  Background:
    Given navigate to "Commission" edit application

  @downloadProposalAndChangeTitleAndCreateMilestoneAndDeleteProposal
  Scenario: LEOS-4587 [EC] Verify User is able to do download, title change, create milestone and delete proposal
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
    And  create proposal button is displayed and enabled
    When click on create proposal button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    When select template "SJ-023 - Proposal for a Regulation of the European Parliament and of the Council"
    Then next button is enabled
    When click on next button
    Then "Create new legislative document - Document metadata (2/2)" is displayed
    When provide document title "Regression Testing" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    When click on download button
    When click on title of the mandate
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " Title change" keyword in the title of the proposal/mandate
    And  click on title save button
    Then message "Metadata saved" is displayed
    And  sleep for 20000 milliseconds
    Then title of the proposal/mandate contains "Regression Testing Title change" keyword
    When click on add button in milestones section
    Then Add a milestone window is displayed
    When click on milestone dropdown icon
    Then "For Interservice Consultation" option is selected by default
    Then milestone title textbox is disabled
    Then these are below options displayed for milestone dropdown
      | For Interservice Consultation |
      | For Decision |
      | Revision after Interservice Consultation |
      | Other |
    When click on milestone option as Other
    And  type "Commission proposal" in title box
    When click on "Create milestone" button
    Then "Milestone creation has been requested" message is displayed
    Then Proposal Viewer screen is displayed
    And  "Commission proposal" is showing in row 1 of title column in milestones table
    And  today's date is showing in date column of milestones table
    And  "In preparation" is showing in status column of milestones table
    And  "File ready" is showing in status column of milestones table
    When click on delete button
    Then proposal deletion confirmation page should be displayed
    And  cancel button is displayed and enabled in proposal deletion confirmation pop up
    And  delete button is displayed and enabled in proposal deletion confirmation pop up
    When click on delete button present in confirmation pop up
    Then navigate to Repository Browser page
    And  close the browser
