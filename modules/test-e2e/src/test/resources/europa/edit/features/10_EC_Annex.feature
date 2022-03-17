#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Annexes in EC instance of Edit Application
@AnnexesRegressionScenariosEditCommission
Feature: Annexes Page Regression Features in Edit Commission

  Background:
    Given navigate to "Commission" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then  navigate to Repository Browser page

  @create_delete_Annex
  Scenario: LEOS-XXXX [EC] Create annexes in Proposal Viewer Page
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
    And  explanatory memorandum section is present
    And  legal act section is present
    And  annexes section is present
    When click on add a new annex button
    Then "Annex" is added to Annexes
    Then numbers of annex present in proposal viewer screen is 1
    When click on title of the Annex 1
    Then title save button of Annex 1 is displayed and enabled
    And  title cancel button of Annex 1 is displayed and enabled
    When add title "Annex 1 Title" to Annex 1
    And  click on title save button of Annex 1
    Then "Annex metadata updated" message is displayed
    Then title of Annex 1 contains "Annex 1 Title"
    When click on add a new annex button
    Then "Annex II" is added to Annexes
    Then "Annex" is changed to "Annex I"
    Then numbers of annex present in proposal viewer screen is 2
    When click on title of the Annex 2
    When add title "Annex 2 Title" to Annex 2
    And  click on title save button of Annex 2
    Then "Annex metadata updated" message is displayed
    Then title of Annex 2 contains "Annex 2 Title"
    When click on add a new annex button
    Then "Annex III" is added to Annexes
    Then numbers of annex present in proposal viewer screen is 3
    When click on title of the Annex 3
    When add title "Annex 3 Title" to Annex 3
    And  click on title save button of Annex 3
    Then "Annex metadata updated" message is displayed
    Then title of Annex 3 contains "Annex 3 Title"
    When click on open button of Annex 1
    Then "Annex" Annex page is displayed
    And  navigation pane is displayed
    And  toc editing button is available
    And  preface and body is present in annex navigation pane
    And  3 level is present in the body of annex page
    When click on toc edition
    Then save button in navigation pane is disabled
    Then save and close button in navigation pane is disabled
    Then cancel button in navigation pane is displayed and enabled
    Then below element lists are displayed in Elements menu
      | Part         |
      | Title        |
      | Chapter      |
      | Section      |
      | Point 1.     |
      | Paragraph    |
    When click on cancel button in navigation pane
    Then elements menu lists are not displayed
    When click on element 1 in annex
    When click on insert before icon present in show all actions icon of level 1
    Then "Point inserted" message is displayed
    And  total number of level is 4
    When click on element 2 in annex
    When click on insert after icon present in show all actions icon of level 2
    Then "Point inserted" message is displayed
    And  total number of level is 5
    When click on element 1 in annex
    When click on edit icon present in show all actions icon of level 1
    Then ck editor window is displayed
    When append " New Text" at the end of the content of level 1
    And  click on save close button of ck editor
    Then document is saved
    Then ck editor window is not displayed
    And  "New Text" is added to content of level 1
    When click on element 1 in annex
    When double click on level 1
    Then ck editor window is displayed
    When remove "New Text" from the content of level 1
    And  click on save close button of ck editor
    Then document is saved
    And  "New Text" is removed from content of level 1
    When click on element 3 in annex
    When click on delete icon present in show all actions icon of level 3
    And  sleep for 2000 milliseconds
    Then "Point deleted" message is displayed
    And  total number of level is 4
    When click on close button present in annex page
    Then Proposal Viewer screen is displayed
    When click on delete button of annex 3
    Then "Annex deletion: confirmation" window is displayed
    When click on delete button in annex deletion confirmation page
    Then "Annex has been deleted" message is displayed
    Then numbers of annex present in proposal viewer screen is 2
    And  close the browser

