#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities of importing office journal in CN instance of Edit Application
@importOfficeJournalEditCouncil
Feature: importing office journal Regression Features in Edit Council

  Background:
    Given navigate to "Council" edit application
    When enter username "user.support.1.name" and password "user.support.1.pwd"
    Then navigate to Repository Browser page
    When click on "Create mandate" button
    Then upload screen is showing with "Create new mandate - Upload a leg file (1/2)" page
    When upload a leg file for creating mandate from location "upload/PROP_ACT_4211964858226529005.leg"
    Then file name should be displayed in upload window
    And  valid icon should be displayed in upload window
    When click on "Next" button
    Then upload screen is showing with "Create new mandate - Document metadata (2/2)" page
    When click on "Create" button
    Then navigate to Repository Browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    And  navigation pane is displayed
    And  legal act content is displayed
    Then toc editing button is available
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on actions hamburger icon
    Then below options are displayed
      | Versions                               |
      | Save this version                      |
      | Download this version                  |
      | Download this version with annotations |
      | Download clean version                 |
      | Export to eConsilium                   |
      | Import                                 |
      | Import from the Official Journal       |
      | View                                   |
      | See user guidance                      |
      | See navigation pane                    |
    And  "See navigation pane" option is checked
    When click on "Import from the Official Journal" option
    Then "Import from the Official Journal of the European Union" window is displayed
    And  below options are displayed in Type dropdown
      | REGULATION |
      | DIRECTIVE  |
      | DECISION   |
    And  "REGULATION" option is selected by default in Type field
    And  current year is selected by default for Year field
    And  blank input box is present for Nr. field
    And  Search button is displayed and enabled
    And  select all recitals button is displayed but disabled
    And  select all articles button is displayed but disabled
    And  import button is displayed but disabled
    And  close button in import office journal window is displayed and enabled
    When mouse hover on i button
    Then tooltip contains messages "Please fill in your search criteria in order to" and "search and import the official journal"

  @importOfficeJournal
  Scenario: LEOS-4516 import from office journal
    When click on search button in import office journal window
    Then border of input box is showing as "rgba(237, 71, 59, 1)" color
    And  exclamation mark is appeared with "rgba(237, 71, 59, 1)" color
    When select option "DIRECTIVE" in Type field
    And  select option "2016" in Year field
    And  provide value "2102" in Nr. field
    And  click on search button in import office journal window
    Then bill content is appeared in import window
    And  checkbox is available beside to each recital
    And  checkbox is available beside to each article
    When click on checkbox of recital 1
    When click on checkbox of recital 2
    When click on checkbox of recital 3
    When click on checkbox of article 1
    When click on checkbox of article 2
    When click on checkbox of article 3
    When click on import button
    And  sleep for 2000 milliseconds
    Then 3 recitals are added at the end of the recitals part
    Then 3 articles are added at the end of the articles part
    When click on actions hamburger icon
    When click on "Import from the Official Journal" option
    Then "Import from the Official Journal of the European Union" window is displayed
    When select option "REGULATION" in Type field
    And  select option "2014" in Year field
    And  provide value "9999" in Nr. field
    And  click on search button in import office journal window
    Then message "Search returned with no result! Please modify the search parameters" is displayed
    When select option "REGULATION" in Type field
    And  select option "2018" in Year field
    And  provide value "643" in Nr. field
    And  click on search button in import office journal window
    Then bill content is appeared in import window
    And  checkbox is available beside to each recital
    And  checkbox is available beside to each article
    When click on select all recitals button in import office journal window
    Then checkboxes of all the recitals are selected
    And  number of recitals selected is 16
    When click on import button
    And  sleep for 2000 milliseconds
    Then 19 recitals are added at the end of the recitals part
    When click on actions hamburger icon
    When click on "Import from the Official Journal" option
    Then "Import from the Official Journal of the European Union" window is displayed
    When select option "REGULATION" in Type field
    And  select option "2018" in Year field
    And  provide value "643" in Nr. field
    And  click on search button in import office journal window
    Then bill content is appeared in import window
    And  checkbox is available beside to each recital
    And  checkbox is available beside to each article
    When click on select all articles button in import office journal window
    Then checkboxes of all the articles are selected
    And  number of articles selected is 13
    When click on import button
    And  sleep for 2000 milliseconds
    Then 16 articles are added at the end of the articles part
    When click on versions pane accordion
    And  click on show more button in recent changes section inside version pane
    Then 3 last technical versions are the imports from office journal
    And  close the browser