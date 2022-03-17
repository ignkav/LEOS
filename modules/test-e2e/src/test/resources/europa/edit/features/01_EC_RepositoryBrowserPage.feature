#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Repository Browser Page in EC instance of Edit Application
@RepositoryBrowserPageRegressionScenariosEditCommission
Feature: Repository Browser Page Regression Features in Edit Commission

  Background:
    Given navigate to "Commission" edit application

  @uploadFileNotVisibleforNonSupportUser
  Scenario: LEOS-4892 [EC] Upload button is not available for non support user in repository browser page
    When enter username "user.nonsupport.1.name" and password "user.nonsupport.1.pwd"
    Then navigate to Repository Browser page
    And  upload button is not present in Repository Browser page
    And  close the browser

  @searchAndOpenProposalByDoubleClick
  Scenario: LEOS-3841 [EC] search a proposal in Repository Browser Page
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
    And  filter section is present
    And  search bar is present
    And  upload button is present
    And  user name is present in the Top right upper corner
    And  proposal/mandate list is displayed
    When untick "Proposal for a regulation" in act category under filter section
    Then "Proposal for a regulation" in act category is unticked
    When click on reset button
    Then "Proposal for a regulation" is ticked in act category under filter section
    When click on minimize application header button
    Then application header is minimized
    When click on maximize application header button
    Then application header is maximized
    When search keyword "Auto_Testing" in the search bar of repository browser page
    Then each proposal/mandate in the search results contain keyword "Auto_Testing"
    When double click on first proposal
    Then Proposal Viewer screen is displayed
    And  close the browser