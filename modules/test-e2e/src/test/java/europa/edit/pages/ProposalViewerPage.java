/*
 * Copyright 2021 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class ProposalViewerPage {
    public static final By TITLE_ELEMENT = By.xpath("//*[contains(@class,'v-slot-doc-purpose')]//input");
    public static final By MILESTONE_ADD_ICON = By.xpath("//*[text()='Milestones']//ancestor::div[contains(@class,'ui-block-heading')]//span[contains(@class,'v-icon')]");
    public static final By MILESTONE_DROPDOWN_ICON = By.xpath("//td[@class='v-formlayout-contentcell']/div[@role='combobox']/div[@role='button']");
    public static final By MILESTONE_OPTIONS_SELECTED = By.xpath("//div[@role='dialog']/following-sibling::div[@role='list']/div[@class='popupContent']//table/tbody/tr/td[@role='listitem' and contains(@class,'selected')]/span");
    public static final By MILESTONE_TITLE_TEXTAREA = By.xpath("//*[text()='Add a milestone']/parent::div/following-sibling::div[1]//textarea");
    public static final By MILESTONE_OPTION_OTHER = By.xpath("//div[@role='dialog']/following-sibling::div[@role='list']/div[@class='popupContent']//table/tbody/tr/td[@role='listitem']/span[text()='Other']");
    public static final By DELETE_BTN = By.xpath("//*[text()='Delete']");
    public static final By MANDATE_DELETION_CONFIRMATION_POPUP = By.xpath("//*[@class='v-window-header' and text()='Mandate deletion: confirmation']");
    public static final By CONFIRM_POPUP_DELETE_BTN = By.xpath("//*[@class='v-window-contents']//span[text()='Delete']");
    public static final By PROPOSAL_DELETION_CONFIRMATION_POPUP = By.xpath("//*[@class='v-window-header' and text()='Proposal deletion: confirmation']");
    public static final By LEGAL_ACT_OPEN_BUTTON = By.xpath("(//*[text()='Legal Act']//ancestor::div[contains(@class,'ui-block ')]//child::div[contains(@class,'v-slot-ui-block-content')])[1]//*[text()='Open']");
    public static final By EXP_MEMO_OPEN_BUTTON = By.xpath("(//*[text()='Explanatory Memorandum']//ancestor::div[contains(@class,'ui-block ')]//child::div[contains(@class,'v-slot-ui-block-content')])[1]//*[text()='Open']");
    public static final By ADD_NEW_EXPLANATORY_BUTTON = By.xpath("//*[text()='Council Explanatory']//parent::div[@class='v-slot']//following-sibling::div//span[contains(@class,'Vaadin-Icons')]");
    public static final By COUNCIL_EXPLANATORY_TABLE_TBODY_TR = By.xpath("//*[contains(@class,'v-slot-explanatory-block')]//tbody/tr");
    public static final By DEFAULT_EXPLANATORY_DELETE_BUTTON_DISABLED = By.xpath("//*[contains(@class,'v-slot-explanatory-block')]//tbody/tr[*]//div[contains(@class,' delete-button') and contains(@class,' v-disabled')]");
    public static final By COUNCIL_EXPLANATORY_CONFIRM_PAGE_CANCEL_BUTTON = By.xpath("//*[text()='Cancel']");
    public static final By COUNCIL_EXPLANATORY_CONFIRM_PAGE_DELETE_BUTTON = By.xpath("//*[@id='confirmdialog-ok-button']//*[text()='Delete']");
    public static final By CLOSE_BUTTON = By.xpath("//*[text()='Close']");
    public static final By ADD_A_MILESTONE_TEXT = By.xpath("//div[text()='Add a milestone']");
    public static final By COLLABORATORS_ADD_BUTTON = By.xpath("//*[text()='Collaborators']//ancestor::div[contains(@class,'ui-block-heading')]//*[contains(@class,'icon-only-add-button')]");
    public static final By COLLABORATORS_SAVE_BUTTON = By.xpath("//*[@class='v-grid-editor-save']");
    public static final By COLLABORATORS_CANCEL_BUTTON = By.xpath("//*[@class='v-grid-editor-cancel']");
    public static final By COLLABORATORS_NAME_1ST_INPUT_BOX = By.xpath("(//div[contains(@class,'v-grid-editor-cells')]//input)[1]");
    public static final By ANNEXES_ADD_BUTTON = By.xpath("//*[text()='Annexes']//ancestor::div[contains(@class,'ui-block-heading')]//*[contains(@class,'icon-only-create-annex-button')]");
    public static final By ANNEXES = By.xpath("//*[text()='Annexes']");
    public static final By CLOSE_BTN = By.xpath("//*[text()='Close']");
    public static final By PROPOSALVIEWERTEXT = By.xpath("//*[text()='Proposal Viewer']");
    public static final By EXPORT_BTN = By.xpath("//*[text()='Export']");
    public static final By DOWNLOAD_BTN = By.xpath("//*[text()='Download']");
    public static final By EXPLN_MEMORANDUM_TEXT = By.xpath("//div[text()='Explanatory Memorandum']");
    public static final By LEGALACTTEXT = By.xpath("//div[text()='Legal Act']");
    public static final By ANNEXESTEXT = By.xpath("//div[text()='Annexes']");
    public static final By COLLABORATORSTEXT = By.xpath("//div[text()='Collaborators']");
    public static final By MILESTONESTEXT = By.xpath("//div[text()='Milestones']");
    public static final By HOME_BTN = By.xpath("//div[@class='home-icon']");
    public static final By CONFIRM_POPUP_CANCEL_BTN = By.xpath("//*[@class='v-window-contents']//span[text()='Cancel']");
    public static final By MILESTONE_ACTIONS_MENU_ITEM = By.xpath("//*[contains(@src,'version_actions.png')]");
    public static final By MILESTONE_SEND_COPY_FOR_REVISION = By.xpath("//*[text()='Send a copy for revision']");
    public static final By SHARE_MILESTONE_WINDOW = By.xpath("//*[text()='Share milestone']");
    public static final By SHARE_MILESTONE_TARGET_USER_INPUT = By.xpath("//*[text()='Target user']/ancestor::tr//input");
    public static final By SEND_FOR_REVISION_BUTTON = By.xpath("//*[text()='Send for revision']//ancestor::div[@role='button']");
    public static final By SEND_FOR_REVISION_DISABLED_BUTTON = By.xpath("//*[text()='Send for revision']//ancestor::div[@role='button' and @aria-disabled='true']");
    public static final By SHARE_MILESTONE_CLOSE_BUTTON = By.xpath("//*[text()='Share milestone']//ancestor::div[@class='popupContent']//*[text()='Close']");
    public static final By CLONED_LABELS = By.xpath("//*[contains(@class,'v-label-cloned-labels')]");
    public static final By MILESTONE_ACTIONS_MENU_ITEM_CAPTION = By.xpath("//*[@class='v-menubar-popup']//span[@class='v-menubar-menuitem-caption']");
    public static final By PROPOSAL_DOWNLOAD_MESSAGE = By.xpath("//*[@class='v-Notification-description' and text()='Proposal downloaded']");

    public static final String GWT_HTML = "//div[contains(@class,'gwt-HTML')]";
    public static final String MILESTONE_TABLE_TBODY_TR = "//table[@role='treegrid']//tbody/tr";
    public static final String MILESTONE_DROPDOWN_LIST_TR = "//div[@role='dialog']/following-sibling::div[@role='list']/div[@class='popupContent']//table/tbody/tr";
    public static final String MILESTONE_DROPDOWN_LIST_TD_SPAN = "//td[@role='listitem']/span";
    public static final String COUNCIL_EXPLANATORY_TABLE_THEAD_TR_TH = "//*[contains(@class,'v-slot-explanatory-block')]//thead/tr/th";
    public static final String COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING = "//*[contains(@class,'v-slot-explanatory-block')]//tbody/tr";
    public static final String COLLABORATORS_TABLE_TBODY_TR = "//*[text()='Collaborators']//ancestor::div[contains(@class,'v-slot-collaborator-block')]//table/tbody//tr";
    public static final String COLLABORATORS_SEARCH_RESULTS_TR = "//*[@id='VAADIN_COMBOBOX_OPTIONLIST']//table/tbody/tr";
    public static final String ANNEX_BLOCK = "//*[contains(@class,'v-slot-annex-block')]";
    public static final String ANNEX_TITLE_INPUT = "//input[@placeholder='Enter annex title']";
    public static final String TITLE_SAVE_BTN = "//div[contains(@class,'save-btn')]";
    public static final String TITLE_CANCEL_BTN = "//div[contains(@class,'cancel-btn')]";
    public static final String FONTAWESOME = "//span[contains(@class,'FontAwesome')]";
    public static final String DELETE_BUTTON_NOT_DISABLED = "//div[contains(@class,' delete-button') and not(contains(@class,' v-disabled'))]";
    public static final String ROLE_BUTTON = "//div[@role='button']";
    public static final String V_BUTTON_CAPTION = "//span[@class='v-button-caption']";
    public static final String OPEN_TEXT = "//*[text()='Open']";
    public static final String ICON_ONLY_DELETE_BUTTON = "//*[contains(@class,'icon-only-delete-button')]";
    public static final String INPUT = "//input";
    public static final String SAVE_BUTTON = "//div[contains(@class,'save-btn') and @role='button']";
    public static final String CANCEL_BUTTON = "//div[contains(@class,'cancel-btn') and @role='button']";
    public static final String ECONSILIUM_TABLE_TBODY_TR = "//table[@role='grid']//tbody/tr";
    public static final String XPATH_TEXT_1 = "//*[text()='";
    public static final String XPATH_TEXT_2 = "']";
}
