#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in Legal Act Page in CN instance of Edit Application
@LegalActRegressionScenariosEditCouncil
Feature: Legal Act Page Regression Features in Edit Council

  @LegalActScenario_Citation
  Scenario: LEOS-4146 CN Edition of elements - Browse Legal Act Citation Part
    Given navigate to "Council" edit application
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
    When click on open button of legal act
    Then legal act page is displayed
    And  navigation pane is displayed
    And  legal act content is displayed
    Then toc editing button is available
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on preamble toggle link
    When click on citation link present in navigation pane
    And  double click on citation 1
    Then ck editor window is displayed
    And  get text from ck editor text box
    When add "New Text" and delete "Treaty " in the ck editor text box
    And  click on save close button of ck editor
    Then document is saved
    And  "New Text" is added in the text box
    And  "Treaty" is deleted with strikeout symbol in the text box
    When mouseHover and click on show all action button and click on edit button of citation 2
    Then ck editor window is displayed
    And  get text from ck editor text box
    When click on cancel button of ck editor
    Then ck editor window is not displayed
    When click on preamble text present in TOC
    When select content in citation 2
    Then comment, suggest and highlight buttons are not displayed
    When click on annotation pop up button
    Then suggest button is displayed
    Then highlight button is displayed
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "citation comment" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "citation comment" is showing in the comment text box
    And  switch from iframe to main window
    When select content in citation 3
    Then comment button is displayed
    Then highlight button is displayed
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggest" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "suggest" is showing in the suggest text box
    And  "Accept" button is showing in suggest text box
    And  "Reject" button is showing in suggest text box
    And  "Comment" button is showing in suggest text box
    And  switch from iframe to main window
    When select content in citation 4
    Then comment button is displayed
    Then suggest button is displayed
    When click on highlight button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then highlight text box is displayed
    When mouse hover on highlight text box
    When click on edit button on highlight box
    Then highlight rich textarea is displayed
    When switch to "highlight" rich textarea iframe
    When enter "citation highlight" in highlight box rich textarea
    And  switch to parent frame
    And  click on "highlight" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "highlight" publish button
    Then "citation highlight" is showing in the highlight text box
    When mouse hover on comment text box
    When click on delete icon of comment text box
    When click on ok button present in windows alert pop up
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment text box is not present
    When mouse hover on highlight text box
    When click on delete icon of highlight text box
    When click on ok button present in windows alert pop up
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then highlight text box is not present
    When click on reject button of suggest text box
    When click on ok button present in windows alert pop up
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest text box is not present
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on preamble toggle link
    When click on the first preamble formula
    Then comment, suggest and highlight buttons are not displayed
    When select content on first preamble formula
    Then comment button is displayed
    Then suggest button is disabled
    Then highlight button is displayed
    When click on the first preamble formula
    Then comment, suggest and highlight buttons are not displayed
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    And  close the browser

  @LegalActScenario_Recital
  Scenario: LEOS-4146 CN Edition of elements - Browse Legal Act Recital Part
    Given navigate to "Council" edit application
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
    When click on open button of legal act
    Then legal act page is displayed
    And  navigation pane is displayed
    And  legal act content is displayed
    Then toc editing button is available
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on preamble toggle link
    When click on recital link present in navigation pane
    And  double click on recital 1
    Then ck editor window is displayed
    And  get text from ck editor text box
    When add "New Text" and delete "cross-border" in the ck editor text box
    And  click on save close button of ck editor
    Then document is saved
    And  "New Text" is added in the text box
    And  "cross-border" is deleted with strikeout symbol in the text box
    When mouseHover and click on show all action button and click on edit button of recital 2
    Then ck editor window is displayed
    And  get text from ck editor text box
    When click on cancel button of ck editor
    Then ck editor window is not displayed
    When click on recital link present in navigation pane
    When select content in recital 1
    When click on annotation pop up button
    Then suggest button is displayed
    Then highlight button is displayed
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "recital comment" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "recital comment" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 2
    Then comment button is displayed
    Then suggest button is disabled
    When click on highlight button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then highlight text box is displayed
    When mouse hover on highlight text box
    When click on edit button on highlight box
    Then highlight rich textarea is displayed
    When switch to "highlight" rich textarea iframe
    When enter "recital highlight" in highlight box rich textarea
    And  switch to parent frame
    And  click on "highlight" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "highlight" publish button
    Then "recital highlight" is showing in the highlight text box
    And  switch from iframe to main window
    When select content in recital 3
    Then comment button is displayed
    Then highlight button is displayed
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggest" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "suggest" is showing in the suggest text box
    And  "Accept" button is showing in suggest text box
    And  "Reject" button is showing in suggest text box
    And  "Comment" button is showing in suggest text box
    And  switch from iframe to main window
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    And  close the browser

  @LegalActScenario_Article
  Scenario: LEOS-4146 CN Edition of elements - Browse Legal Act Article Part
    Given navigate to "Council" edit application
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
    When click on open button of legal act
    Then legal act page is displayed
    And  navigation pane is displayed
    And  legal act content is displayed
    Then toc editing button is available
    When click on toggle bar move to right
    Then toggle bar moved to right
    When click on toc edition
    Then save button in navigation pane is disabled
    Then save and close button in navigation pane is disabled
    Then cancel button in navigation pane is displayed and enabled
    Then below element lists are displayed in Elements menu
      | Citation     |
      | Recital      |
      | Part         |
      | Title        |
      | Chapter      |
      | Section      |
      | Article      |
      | Paragraph    |
      | Subparagraph |
      | Point (a)    |
    When click on article 1 in navigation pane
    Then selected element section is displayed
    Then input value "Article" for element Type is disabled in selected element section
    Then input value 1 for element Number is disabled in selected element section
    Then input value "Scope" for element Heading is editable in selected element section
    Then Paragraph Numbering has below options
      | Numbered   |
      | Unnumbered |
    And  both the options of Paragraph Numbering are editable
    Then delete button is displayed and enabled in selected element section
    When click on cross symbol of the selected element
    Then selected element section is not displayed
    When click on "cancel" button present in navigation pane
    Then save button in navigation pane is not displayed
    Then save and close button in navigation pane is not displayed
    Then cancel button in navigation pane is not displayed
    Then elements section attached to navigation pane is not displayed
    When click on article 1 in navigation pane
    And  double click on paragraph 1
    Then ck editor window is displayed
    And  get text from ck editor li text box
    When add "New Text" and delete "establishes " in the ck editor li text box
    And  click on save close button of ck editor
    Then document is saved
    And  "New Text" is added in the text box
    And  "establishes" is deleted with strikeout symbol in the text box
    When mousehover and click on show all action button and click on edit button of point 1
    Then ck editor window is displayed
    And  get text from ck editor li text box
    When click on cancel button of ck editor
    Then ck editor window is not displayed
    When click on article 1 in navigation pane
    When select content in point 1
    Then comment, suggest and highlight buttons are not displayed
    When click on annotation pop up button
    Then suggest button is displayed
    Then highlight button is displayed
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "point comment" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "point comment" is showing in the comment text box
    And  switch from iframe to main window
    When select content in point 2
    Then comment button is displayed
    Then highlight button is displayed
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "suggest" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "suggest" is showing in the suggest text box
    And  "Accept" button is showing in suggest text box
    And  "Reject" button is showing in suggest text box
    And  "Comment" button is showing in suggest text box
    And  switch from iframe to main window
    When select content in point 3
    Then comment button is displayed
    Then suggest button is displayed
    When click on highlight button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then highlight text box is displayed
    When mouse hover on highlight text box
    When click on edit button on highlight box
    Then highlight rich textarea is displayed
    When switch to "highlight" rich textarea iframe
    When enter "point highlight" in highlight box rich textarea
    And  switch to parent frame
    And  click on "highlight" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "highlight" publish button
    Then "point highlight" is showing in the highlight text box
    And  switch from iframe to main window
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    And  close the browser

  @exportToEConsiliumAllTextAndAnnotation
  Scenario: LEOS-XXXX export to eConsilium in legal act page with all text and annotations
    Given navigate to "Council" edit application
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
    When click on open button of legal act
    Then legal act page is displayed
    And  navigation pane is displayed
    And  legal act content is displayed
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
    When click on "Export to eConsilium" option
    Then "Export to eConsilium" window is displayed
    And  "All text" option is ticked in Export to eConsilium window
    When provide title "Export to eConsilium Testing" in Export to eConsilium window
    And  tick "With Annotations" option in Export to eConsilium window
    And  click on export button in Export to eConsilium window
    Then sleep for 2000 milliseconds
    When click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    Then "Export to eConsilium Testing" is showing under title column row 1 in Export to eConsilium section
    And  today's date is showing under date column row 1 in Export to eConsilium section
    And  "in progress" is showing under status column row 1 in Export to eConsilium section
    When click on close button present in proposal viewer page
    Then navigate to Repository Browser page
    And  close the browser