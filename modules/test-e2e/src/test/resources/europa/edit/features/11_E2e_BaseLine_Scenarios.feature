#Author: Satyabrata Das
#Keywords Summary : Testing for BaseLine scenarios for Leos
@BaseLineScenariosCommission
Feature: BaseLine Scenarios in Edit Commission

  @E2eAnnexTesting
  Scenario: Checks the integrity of the Proposal during basic operations Add/Delete Article/Level
    Given navigate to "Commission" edit application
    Then navigate to Repository Browser page
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
    When click on open button of legal act
    Then legal act page is displayed
    When click on article 1 in navigation pane
    Then article 1 is displayed in bill content
    When click on insert before icon present in show all actions icon of article 1
    Then "A new article has been inserted" message is displayed
    And  click on message "A new article has been inserted"
    And  total number of article is 3 in enacting terms
    When click on toc edition
    When click on cross symbol of the selected element
    When click on article 2 in navigation pane
    Then selected element section is displayed
    When click on delete button present in selected element section
    When click on save and close button in navigation pane
    Then message "Document structure saved." is displayed
    And  click on message "Document structure saved."
    And  total number of article is 2 in enacting terms
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When click on add a new annex button
    Then "Annex" is added to Annexes
    Then numbers of annex present in proposal viewer screen is 1
    When click on open button of Annex 1
    Then "Annex" Annex page is displayed
    When click on element 1 in annex
    When click on insert before icon present in show all actions icon of level 1
    Then "Point inserted" message is displayed
    And  click on message "Point inserted"
    And  total number of level is 4
    When click on toc edition
    When click on cross symbol of the selected element
    When click on element 2 in annex
    Then selected element section is displayed
    When click on delete button present in selected element section
    When click on save and close button in navigation pane
    Then message "Document structure saved." is displayed
    And  click on message "Document structure saved."
    And  total number of level is 3
    When click on close button present in annex page
    Then Proposal Viewer screen is displayed
    When click on download button
    When find the recent "zip" file in download path and unzip it in "upload" and get the latest "leg" file
    When click on home button
    Then navigate to Repository Browser page
    When click on upload button present in the Repository Browser page
    Then upload window 'Upload a leg file 1/2' is showing
    When upload a latest "leg" file for creating proposal from location "upload"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on next button
    Then "Upload a legislative document - Document metadata (2/2)" is displayed
    And  click on create button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    Then title of the proposal/mandate contains "Automation Testing" keyword
    Then numbers of annex present in proposal viewer screen is 1
    When click on open button of legal act
    Then legal act page is displayed
    And  total number of article is 2 in enacting terms
    And  heading of Article 1 is "Article heading..."
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When click on open button of Annex 1
    Then "Annex" Annex page is displayed
    And  total number of level is 3
    When click on close button present in annex page
    Then Proposal Viewer screen is displayed
    When click on close button present in proposal viewer page
    Then navigate to Repository Browser page
#    And  delete all the proposal containing keyword
#      | Automation |
    And  close the browser

#  @deleteProposal
#  Scenario: delete proposal
#    Given navigate to "Commission" edit application
#    Then navigate to Repository Browser page
#    And  delete all the proposal containing keyword
#      | Automation |
#    And  close the browser