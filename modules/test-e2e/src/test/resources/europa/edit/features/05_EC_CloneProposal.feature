#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in clone proposal in EC instance of Edit Application
@CloneProposalRegressionScenarios
Feature: Clone Proposal Regression Features in Edit Commission

  Background:
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page

  @editArticleInCloneProposal
  Scenario: LEOS-5043 [EC] clone a proposal and edit an article in legal act
    And  create proposal button is displayed and enabled
    When click on create proposal button
    Then "Create new legislative document - Template selection (1/2)" window is displayed
    When select template "SJ-023 - Proposal for a Regulation of the European Parliament and of the Council"
    Then next button is enabled
    When click on next button
    Then "Create new legislative document - Document metadata (2/2)" is displayed
    When provide document title "Automation Testing" in document metadata page
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
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
    When click on actions hamburger icon of first milestone
    Then below options are displayed under milestone actions hamburger icon
      | View                     |
      | Send a copy for revision |
    When click on send a copy for revision option
    Then share milestone window is displayed
    And  "Type user name" is mentioned in target user input field
    And  send for revision button is displayed but disabled
    And  close button is displayed and enabled in share milestone window
    When search "DAS Satyabrata" in the target user field
    Then user "DAS Satyabrata" is showing in the list
    When click on first user showing in the list
    Then "DAS Satyabrata" user is selected in the target user input field
    And  send for revision button is displayed and enabled
    When click on send for revision button
    Then "Milestone cloned" message is displayed
    And  sleep for 35000 milliseconds
    Then "Sent for revision to DAS Satyabrata" is showing under title column row 2 of milestones table
    And  today's date is showing under date column row 2 of milestones table
    And  "For revision" is showing under status column row 2 of milestones table
    When click on close button present in proposal viewer page
    Then navigate to Repository Browser page
    And  first proposal name contains "Automation Testing"
    And  colour of first proposal is "rgba(226, 226, 226, 1)"
    And  first proposal contains keyword Revision status: For revision
    And  first proposal contains keyword REVISION EdiT
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    And  proposal title has a label REVISION EdiT in proposal viewer page
    When click on open button of legal act
    Then legal act page is displayed
    When click on article 1 in navigation pane
    And  mouse hover and click on show all action button and click on edit button of article 1
    Then ck editor window is displayed
    And  save close button is disabled in ck editor
    And  save button is disabled in ck editor
    When append "New Text" at the end of the paragraph 1 of the article
    And  click on cancel button of ck editor
    Then confirm cancel editing window is displayed
    When click on ok button in confirm cancel editing window
    Then ck editor window is not displayed
    When click on article 1 in navigation pane
    And  mouse hover and click on show all action button and click on edit button of article 1
    Then ck editor window is displayed
    When append "NewText" at the end of the paragraph 1 of the article
    And  click on save close button of ck editor
    Then document is saved
    Then ck editor window is not displayed
    Then "NewText" is added with colour "rgba(0, 128, 0, 1)" to the paragraph 1 of article 1
    When click on article 2 in navigation pane
    When double click on article 2
    Then ck editor window is displayed
    When append "NewText" at the end of the paragraph 1 of the article
    And  click on save close button of ck editor
    Then document is saved
    Then ck editor window is not displayed
    Then "NewText" is added with colour "rgba(0, 128, 0, 1)" to the paragraph 1 of article 2
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    And  close the browser