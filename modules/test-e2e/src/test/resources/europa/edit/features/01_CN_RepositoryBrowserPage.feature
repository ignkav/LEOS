#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Repository Browser Page in CN instance of Edit Application
@RepositoryBrowserPageRegressionScenariosEditCouncil
Feature: Repository Browser Page Regression Features in Edit Council

  @resetFilterAndSearchProposalAndDoubleClickToOpenTheMandate
  Scenario: LEOS-3841 [CN] Verify user is able to reset filter, search mandate and open proposal by using double click
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
    And  filter section is present
    And  search bar is present
    And  "Create mandate" button is present
    And  user name is present in the Top right upper corner
    And  proposal/mandate list is displayed
    When untick "Proposal for a directive" in act category under filter section
    Then "Proposal for a directive" in act category is unticked
    When click on reset button
    Then "Proposal for a directive" is ticked in act category under filter section
    When search keyword "Auto_Testing" in the search bar of repository browser page
    Then each proposal/mandate in the search results contain keyword "Auto_Testing"
    When double click on first proposal
    Then Proposal Viewer screen is displayed
    When click on home button
    Then navigate to Repository Browser page
    And  close the browser

#  @test
#  Scenario: test
#    When click on "Create mandate" button
#    Then "Upload Screen" is showing with "Create new mandate - Upload a leg file (1/2)" page
#    When upload a leg file for creating mandate from location "council/createMandate/PROP_ACT_3530794399585608473.leg"
#    Then file name should be displayed in upload window
#    And  valid icon should be displayed in upload window
#    When click on "Next" button
#    Then "Upload Screen" is showing with "Create new mandate - Document metadata (2/2)" page
#    When click on "Create" button
#    Then navigate to Repository Browser page
#    And  sleep for 35000 milliseconds
#    When click on the open button of first proposal/mandate
#    Then Proposal Viewer screen is displayed
#    When click on open button of legal act
#    Then legal act page is displayed
#    #When click on annotation pop up button
#    When click on preamble toggle link
#    When click on recital link present in navigation pane
#    When add comment message "recital comment" 100 times to recital 1
    #When select content in recital 1
#    When click on comment button
#    And  switch from main window to iframe "hyp_sidebar_frame"
#    When switch to "comment" rich textarea iframe
#    When enter "recital comment" in comment box rich textarea
#    And  switch to parent frame
#    And  click on "comment" publish button
#    Then "recital comment" is showing in the comment text box
#    And  switch from iframe to main window



