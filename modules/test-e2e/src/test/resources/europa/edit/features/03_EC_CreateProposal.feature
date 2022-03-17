#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Create Proposal Page
@CreateProposalRegressionScenariosEditCommission
Feature: Create Proposal Regression Features in Edit Commission

  Background:
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then  navigate to Repository Browser page

  @createProposalByUploadingAndDownloadingLegFile
  Scenario: LEOS-4897,4517 [EC] Verify user is able to create the proposal successfully
    And  create proposal button is displayed and enabled
    When click on create proposal button
    When select template "SJ-023 - Proposal for a Regulation of the European Parliament and of the Council"
    When click on next button
    Then "Create new legislative document - Document metadata (2/2)" is displayed
    When click on previous button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    And  cancel button is displayed and enabled
    When click on cancel button
    Then navigate to Repository Browser page
    When click on create proposal button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    And  previous button is disabled
    When select template "SJ-023 - Proposal for a Regulation of the European Parliament and of the Council"
    Then next button is enabled
    When click on next button
    Then "Create new legislative document - Document metadata (2/2)" is displayed
    And  previous button is enabled
    When provide document title "Automation Testing" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    And  export button is displayed and enabled
    And  download button is displayed and enabled
    And  delete button is displayed and enabled
    And  close button is displayed and enabled
    And  explanatory memorandum section is present
    And  legal act section is present
    And  annexes section is present
    And  collaborators section is Present
    And  milestones section is present
    When click on download button
    And  sleep for 7000 milliseconds
    Then Proposal Viewer screen is displayed
    When find the recent "zip" file in download path and unzip it in "upload" and get the latest "leg" file
    When click on home button
    Then upload button is present
    When click on upload button present in the Repository Browser page
    Then upload window 'Upload a leg file 1/2' is showing
    When upload a latest "leg" file for creating proposal from location "upload"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on next button
    Then "Upload a legislative document - Document metadata (2/2)" is displayed
    When provide document title "Automation Testing" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    Then title of the proposal/mandate contains "Automation Testing" keyword
    When click on close button
    Then navigate to Repository Browser page
    And  close the browser

  @createProposalByUploadingExistingLegFile
  Scenario: LEOS-XXXX [EC] Verify user is able to create proposal by uploading existing leg file
    When enter username "dasatya" and password "JdbdL1+Bn57TEdgR7ktDHw=="
    Then navigate to Repository Browser page
    When click on upload button present in the Repository Browser page
    Then upload window 'Upload a leg file 1/2' is showing
    When upload a leg file for creating proposal from location "upload/PROP_ACT_2373866412475407095.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on next button
    Then "Upload a legislative document - Document metadata (2/2)" is displayed
    When provide document title "Automation Testing" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    And  close the browser