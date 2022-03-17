#Author: Satyabrata Das
#Keywords Summary : Testing for different functionalities in annotations in EC instance of Edit Application
@annotationsScenariosEditCommission
Feature: Annotation Regression Features in Edit Commission

  @annotationsVisibilityForDifferentDGs
  Scenario: LEOS-4966 [EC] Test scenario- Annotations visibility for different DGs(DG and Presidency Users)
    Given navigate to "Commission" edit application
    When enter username "user.nonsupport.1.name" and password "user.nonsupport.1.pwd"
    Then navigate to Repository Browser page
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
    When click on title of the mandate
    Then title save button is displayed and enabled
    And  title cancel button is displayed and enabled
    When append " Annotation Testing" keyword in the title of the proposal/mandate
    And  click on title save button
    Then title of the proposal/mandate contains "Annotation Testing" keyword
    And  collaborators section is Present
    When click on add collaborator button
    Then collaborator save button is displayed
    And  collaborator cancel button is displayed
    And  search input box is enabled for name column in Collaborator section
    When search "Test2" in the name input field
    Then "Test2" user is showing in the list
    When click on first user showing in the list
    Then "Test2" user is selected in the name input field
    When click on save button in Collaborator section
    Then "Test2" user is showing in the collaborator list
    When click on add collaborator button
    When search "Test6" in the name input field
    Then "Test6" user is showing in the list
    When click on first user showing in the list
    Then "Test6" user is selected in the name input field
    When click on save button in Collaborator section
    Then "Test6" user is showing in the collaborator list
    When click on add collaborator button
    When search "Test7" in the name input field
    Then "Test7" user is showing in the list
    When click on first user showing in the list
    Then "Test7" user is selected in the name input field
    When click on save button in Collaborator section
    Then "Test7" user is showing in the collaborator list
    When click on open button of legal act
    Then legal act page is displayed
    And  navigation pane is displayed
    When click on actions hamburger icon
    When click on "Import from the Official Journal" option
    Then "Import from the Official Journal of the European Union" window is displayed
    When select option "DIRECTIVE" in Type field
    And  select option "2016" in Year field
    And  provide value "2102" in Nr. field
    And  click on search button in import office journal window
    Then bill content is appeared in import window
    When click on checkbox of recital 1
    When click on checkbox of recital 2
    When click on checkbox of recital 3
    When click on checkbox of recital 4
    When click on import button
    Then 4 recitals are added in bill content
    When click on preamble toggle link
    When click on recital link present in navigation pane
    When select content in recital 1
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "DG to DG" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
      | DG            |
    When click on "DG" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "DG to DG" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 2
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "DG to Collaborators" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "DG to Collaborators" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 3
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "DG to Only Me" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Only Me" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "DG to Only Me" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 4
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "DGtoDG" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "DG" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "DGtoDG" is showing in the suggest text box
    And  switch from iframe to main window
    When select content in recital 5
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "DGtoCollaborators" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "DGtoCollaborators" is showing in the suggest text box
    And  switch from iframe to main window
    When select content in recital 6
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "DGtoOnlyMe" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Only Me" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "DGtoOnlyMe" is showing in the suggest text box
    And  switch from iframe to main window
    And  click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Commission" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.2.name" and password "user.nonsupport.2.pwd"
    Then navigate to Repository Browser page
    When search keyword "Annotation Testing" in the search bar of repository browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    When click on preamble toggle link
    When click on recital link present in navigation pane
    When select content in recital 1
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "Presidency to Presidency" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    Then below groups are displayed in the annotation sharing setting list
      | Collaborators |
      | Only Me       |
      | Presidency    |
    When click on "Presidency" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "Presidency to Presidency" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 2
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "Presidency to Collaborators" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "Presidency to Collaborators" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 3
    When click on comment button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then comment box rich text area is displayed
    When switch to "comment" rich textarea iframe
    When enter "Presidency to Only Me" in comment box rich textarea
    And  switch to parent frame
    And  click on "comment" annotation sharing setting
    When click on "Only Me" option in the annotation sharing setting list
    And  click on "comment" publish button
    Then "Presidency to Only Me" is showing in the comment text box
    And  switch from iframe to main window
    When select content in recital 4
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "PresidencytoPresidency" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Presidency" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "PresidencytoPresidency" is showing in the suggest text box
    And  switch from iframe to main window
    When select content in recital 5
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "PresidencytoCollaborators" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Collaborators" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "PresidencytoCollaborators" is showing in the suggest text box
    And  switch from iframe to main window
    When select content in recital 6
    When click on suggest button
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then suggest textarea is displayed
    When enter "PresidencytoOnlyMe" in suggest box textarea
    And  click on "suggest" annotation sharing setting
    When click on "Only Me" option in the annotation sharing setting list
    And  click on "suggest" publish button
    Then "PresidencytoOnlyMe" is showing in the suggest text box
    And  switch from iframe to main window
    And  click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Commission" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.3.name" and password "user.nonsupport.3.pwd"
    Then navigate to Repository Browser page
    When search keyword "Annotation Testing" in the search bar of repository browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    When click on toggle bar move to left
    Then toggle bar moved to left
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then below comments are showing in the comment text boxes
      | DG to DG            |
      | DG to Collaborators |
    Then below suggestions are showing in the suggestion text boxes
      | DGtoDG            |
      | DGtoCollaborators |
    And  switch from iframe to main window
    And  click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    When double click on minimize maximize button present in the right upper corner of the application
    When click on logout button
    And  redirect the browser to ECAS url
    Then ECAS successful login page is displayed
    When click on logout button in ECAS logged in page
    Then user is logged out from ECAS
    When navigate to "Commission" edit application
    Then sign in with a different e-mail address page is displayed
    When click on sign in with a different e-mail address hyperlink
    Then sign in to continue page is displayed
    When enter username "user.nonsupport.4.name" and password "user.nonsupport.4.pwd"
    Then navigate to Repository Browser page
    When search keyword "Annotation Testing" in the search bar of repository browser page
    When click on the open button of first proposal/mandate
    Then Proposal Viewer screen is displayed
    When click on open button of legal act
    Then legal act page is displayed
    When click on toggle bar move to left
    Then toggle bar moved to left
    And  switch from main window to iframe "hyp_sidebar_frame"
    Then below comments are showing in the comment text boxes
      | Presidency to Presidency    |
      | Presidency to Collaborators |
    Then below suggestions are showing in the suggestion text boxes
      | PresidencytoPresidency    |
      | PresidencytoCollaborators |
    And  switch from iframe to main window
    And  click on close button present in legal act page
    Then Proposal Viewer screen is displayed
    And  close the browser