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
public class LegalActPage {
    public static final By CITATION_LINK = By.xpath("//*[text()='Citations']");
    public static final By CITATION_SECOND_PARAGRAPH = By.xpath("//citation[2]/aknp");
    public static final By LEGAL_ACT_TEXT = By.xpath("//span[text()='Legal Act']");
    public static final By PREAMBLE_TOGGLE_LINK = By.xpath("//*[text()='Preamble']//preceding-sibling::span");
    public static final By CK_EDITOR_WINDOW = By.xpath("//*[contains(@id,'cke_editor')]");
    public static final By CITATION_BEFORE_CKEDITOR = By.xpath("//p//ancestor::div[contains(@class,'cke_editable')]//preceding-sibling::citation");
    public static final By CKEDITOR_CANCEL_BUTTON = By.xpath("//*[contains(@class,'cke_button__leosinlinecancel_icon')]");
    public static final By CKEDITOR_SAVECLOSE_BUTTON = By.xpath("//*[contains(@class,'cke_button__leosinlinesaveclose_icon')]");
    public static final By CLOSE_BUTTON = By.xpath("//*[text()='Close']");
    public static final By NEWTEXT_INSIDE_CKEDITOR_CLASS = By.xpath("//span[@class='leos-content-soft-new']");
    public static final By DELETEDTEXT_INSIDE_CKEDITOR_CLASS = By.xpath("//span[@class='leos-content-soft-removed']");
    public static final By CK_EDTOR_PARAGRAPH_INNERTEXT = By.xpath("//*[contains(@class,cke_editable_inline) and @role='textbox']//p");
    public static final By CK_EDTOR_LI_INNERTEXT = By.xpath("//*[contains(@class,cke_editable_inline) and @role='textbox']//li");
    public static final By TOGGLE_BAR_IN_LEFT_SIDE = By.xpath("//button[@title='Toggle or Resize Sidebar' and contains(@class,'h-icon-chevron-right')]");
    public static final By TOGGLE_BAR_IN_RIGHT_SIDE = By.xpath("//button[@title='Toggle or Resize Sidebar' and contains(@class,'h-icon-chevron-left')]");
    public static final By DOCUMENT_SAVED_TEXT = By.xpath("//*[text()='Document saved']");
    public static final By TOC_EDIT_BUTON = By.xpath("//img[contains(@src,'toc-edit.png')]");
    public static final By PREAMBLE_FORMULA_AKNP = By.xpath("//preamble/formula[1]/aknp");
    public static final By PREAMBLE_TEXT = By.xpath("//div[text()='Preamble']");
    public static final By COMMENT_TEXTBOX = By.xpath("//*[contains(@class,'thread-list__card is-comment')]");
    public static final By COMMENT_RICH_TEXTAREA = By.xpath("//*[contains(@class,'is-comment')]//div[@ng-show='vm.showEditor()' and not(@class='ng-hide')]//div[contains(@id,'cke_editor')]");
    public static final By COMMENT_HIGHLIGHT_RICH_TEXTAREA_PARAGRAPH = By.xpath("//*[contains(@class,'cke_editable_themed')]/p");
    public static final By COMMENT_TEXTBOX_DELETE_BUTTON = By.xpath("//*[contains(@class,'thread-list__card is-comment')]//i[@class='h-icon-annotation-delete btn-icon']");
    public static final By COMMENT_ARROW_DOWN_BUTTON = By.xpath("//div[contains(@class,'is-comment')]//button[@class='dropdown-menu-btn__dropdown-arrow']");
    public static final By COMMENT_PUBLISH_BUTTON = By.xpath("//div[contains(@class,'is-comment')]//button[@class='dropdown-menu-btn__btn ng-binding']");
    public static final By COMMENT_TEXTAREA_PARAGRAPH_INNERTEXT = By.xpath("//div[contains(@class,'is-comment')]//div[@class='markdown-body js-markdown-preview has-content']/p");
    public static final By SUGGESTION_TEXTBOX = By.xpath("//*[contains(@class,'thread-list__card is-suggest')]");
    public static final By SUGGESTION_TEXTAREA = By.xpath("//div[contains(@class,'is-suggestion')]//textarea[not(contains(@class,'ng-hide'))]");
    public static final By SUGGESTION_ARROW_DOWN_BUTTON = By.xpath("//div[contains(@class,'is-suggestion')]//button[@class='dropdown-menu-btn__dropdown-arrow']");
    public static final By SUGGESTION_PUBLISH_BUTTON = By.xpath("//div[contains(@class,'is-suggestion')]//button[@class='dropdown-menu-btn__btn ng-binding']");
    public static final By SUGGESTION_TEXTAREA_PARAGRAPH_INNERTEXT = By.xpath("//div[contains(@class,'is-suggestion')]//div[@class='markdown-body js-markdown-preview has-content']/p");
    public static final By SUGGESTION_ACCEPT_BUTTON = By.xpath("//div[contains(@class,'is-suggestion')]//button[@class='suggestion-merge-action']");
    public static final By SUGGESTION_REJECT_BUTTON = By.xpath("//div[contains(@class,'is-suggestion')]//button[@class='publish-annotation-cancel-btn btn-clean']");
    public static final By SUGGESTION_COMMENT_BUTTON = By.xpath("//div[contains(@class,'is-suggestion')]//button[@title='Comment suggestion']");
    public static final By HIGHLIGHT_TEXTBOX = By.xpath("//*[contains(@class,'thread-list__card is-highlight')]");
    public static final By HIGHLIGHT_RICH_TEXTAREA = By.xpath("//*[contains(@class,'is-highlight')]//div[@ng-show='vm.showEditor()' and not(@class='ng-hide')]//div[contains(@id,'cke_editor')]");
    public static final By HIGHLIGHT_TEXTBOX_EDIT_BUTTON = By.xpath("//*[contains(@class,'thread-list__card is-highlight')]//i[@class='h-icon-annotation-edit btn-icon']");
    public static final By HIGHLIGHT_TEXTBOX_DELETE_BUTTON = By.xpath("//*[contains(@class,'thread-list__card is-highlight')]//i[@class='h-icon-annotation-delete btn-icon']");
    public static final By HIGHLIGHT_TEXTAREA = By.xpath("//div[contains(@class,'is-highlight')]//textarea");
    public static final By HIGHLIGHT_ARROW_DOWN_BUTTON = By.xpath("//div[contains(@class,'is-highlight')]//button[@class='dropdown-menu-btn__dropdown-arrow']");
    public static final By HIGHLIGHT_PUBLISH_BUTTON = By.xpath("//div[contains(@class,'is-highlight')]//button[@class='dropdown-menu-btn__btn ng-binding']");
    public static final By HIGHLIGHT_TEXTAREA_PARAGRAPH_INNERTEXT = By.xpath("//div[contains(@class,'is-highlight')]//div[@class='markdown-body js-markdown-preview has-content']/p");
    public static final String PUBLISH_ANNOTATION_UL = "//ul[contains(@class,'publish-annotation-btn__dropdown-menu')]";
    public static final By PUBLISH_ANNOTATION_UL_LI_A = By.xpath("//ul[contains(@class,'publish-annotation-btn__dropdown-menu')]//li//a");
    public static final By TOC_RECITAL_LINK = By.xpath("//*[text()='Recitals']");
    public static final String TOC_TABLE_TREE_GRID = "//table[@role='treegrid']";
    public static final By NAVIGATION_PANE_SAVE_BUTTON = By.xpath("//*[contains(@src,'toc-save')]//ancestor::div[@role='button']");
    public static final By NAVIGATION_PANE_SAVE_AND_CLOSE_BUTTON = By.xpath("//*[contains(@src,'toc-save-close')]//ancestor::div[@role='button']");
    public static final By NAVIGATION_PANE_CANCEL_BUTTON = By.xpath("//*[contains(@src,'toc-cancel')]//ancestor::div[@role='button']");
    public static final By SELECTED_ELEMENT_TEXT = By.xpath("//*[text()='Selected element']");
    public static final By SELECTED_ELEMENT_TYPE_INPUT = By.xpath("//*[text()='Type']//ancestor::tr//input");
    public static final By SELECTED_ELEMENT_NUMBER_INPUT = By.xpath("//*[text()='Number']//ancestor::tr//input");
    public static final By SELECTED_ELEMENT_HEADING_INPUT = By.xpath("//*[text()='Heading']//ancestor::tr//input");
    public static final By SELECTED_ELEMENT_PARAGRAPH_NUMBERING_NUMBERED_INPUT = By.xpath("//*[text()='Paragraph Numbering']//ancestor::div[@class='v-widget v-has-caption v-caption-on-top']//label[text()='Numbered']//ancestor::span//input");
    public static final By SELECTED_ELEMENT_PARAGRAPH_NUMBERING_UNNUMBERED_INPUT = By.xpath("//*[text()='Paragraph Numbering']//ancestor::div[@class='v-widget v-has-caption v-caption-on-top']//label[text()='Unnumbered']//ancestor::span//input");
    public static final By SELECTED_ELEMENT_PARAGRAPH_NUMBERING_LABEL_LIST = By.xpath("//*[text()='Paragraph Numbering']//ancestor::div[@class='v-widget v-has-caption v-caption-on-top']//label");
    public static final By SELECTED_ELEMENT_DELETE_BUTTON = By.xpath("(//*[contains(@class,'leos-toc-editor')])[1]//*[text()='Delete']");
    public static final By SELECTED_ELEMENT_CLOSE_BUTTON = By.xpath("//*[text()='Selected element']//ancestor::div[contains(@class,'leos-slider-toolbar')][1]//*[@role='button']");
    public static final By NAVIGATION_ELEMENTS_LIST = By.xpath("//*[text()='Elements']//ancestor::div[contains(@class,'leos-left-slider-panel')]//div[contains(@class,'leos-drag-item')]");
    public static final By NAVIGATION_ELEMENTS_LEFT_SLIDER_PANEL = By.xpath("//*[text()='Elements']//ancestor::div[contains(@class,'leos-left-slider-panel')]");
    public static final By DELETE_ITEM_CONFIRMATION_TEXT = By.xpath("//*[text()='Delete item: confirmation']");
    public static final By DELETE_ITEM_CONFIRMATION_WINDOW_CONTINUE_BUTTON = By.xpath("//*[text()='Continue']//ancestor::div[@role='button']");
    public static final By LEGAL_ACT_CONTENT = By.xpath("//*[@id='legalTextLayout']");
    public static final By ACTION_MENU = By.xpath("//span[contains(@class,'leos-actions-menu-selector')]");
    public static final By VERSIONS_PANE = By.xpath("//*[text()='Versions pane']");
    public static final By ACTIONS_MENU_BAR_POP_UP = By.xpath("//*[@class='v-menubar-popup']//*[contains(@class,'leos-actions-menu')]//span//span");
    public static final By ANNOTATION_COMMENT_PARAGRAPH = By.xpath("//*[@class='thread-list__card is-comment']//*[contains(@class,'has-content')]//p");
    public static final By ANNOTATION_SUGGESTION_CONTENT_NEW = By.xpath("//*[@class='thread-list__card is-suggestion']//*[contains(@class,'has-content')]//span[@class='leos-content-new']");
    public static final By RECITALS_SOFT_NEW = By.xpath("//recital[@class='leos-content-soft-new']");
    public static final By ARTICLES_SOFT_NEW = By.xpath("//article[@class='leos-content-soft-new']");
    public static final By RECENT_CHANGES_SHOW_MORE_BUTTON = By.xpath("//*[text()='Recent changes']//ancestor::div[@id='versionCard']//*[contains(@class,'show-versions-button') and @role='button']");
    public static final By EXPORT_BUTTON_ECONSILIUM_WINDOW = By.xpath("//*[text()='Export']");
    public static final By TITLE_ECONSILIUM_WINDOW = By.xpath("//*[text()='Title']//ancestor::tr//textarea");
    public static final By SAVE_CLOSE_BUTTON_DISABLED = By.xpath("//a[@title='Save Close' and contains(@class,'disabled')]");
    public static final By SAVE_BUTTON_DISABLED = By.xpath("//a[@title='Save' and contains(@class,'disabled')]");
    public static final By CONFIRM_CANCEL_EDITING = By.xpath("//*[text()='Confirm cancel editing.']");
    public static final By OK_BUTTON = By.xpath("//*[text()='OK']");
    public static final By RECITAL_NEW = By.xpath("//recital[contains(@id,'imp')]");
    public static final By ARTICLE_NEW = By.xpath("//article[contains(@id,'imp')]");
    public static final By SHOW_ALL_ACTIONS_INSERT_BEFORE = By.xpath("//*[@data-widget-type='insert.before' and @style='display: inline-block;']");
    public static final By SHOW_ALL_ACTIONS_INSERT_AFTER = By.xpath("//*[@data-widget-type='insert.after' and @style='transform: rotate(180deg); display: inline-block;']");
    public static final By SHOW_ALL_ACTIONS_EDIT = By.xpath("//*[@data-widget-type='edit' and @style='display: inline-block;']");
    public static final By SHOW_ALL_ACTIONS_DELETE = By.xpath("//*[@data-widget-type='delete' and @style='display: inline-block;']");


    public static final String CITATION = "//citation";
    public static final String RECITAL = "//recital";
    public static final String POINT = "//point";
    public static final String PARAGRAPH = "//paragraph";
    public static final String SUBPARAGRAPH = "//subparagraph";
    public static final String XPATH_TEXT_1 = "//*[text()='";
    public static final String XPATH_TEXT_2 = "']";
    public static final String NG_SHOW_EDITOR = "//div[@ng-show='vm.showEditor()' and not(@class='ng-hide')]";
    public static final String HIGHLIGHT_ANNOTATION = "//*[contains(@class,'is-highlight')]";
    public static final String COMMENT_ANNOTATION = "//*[contains(@class,'is-comment')]";
    public static final String RICH_TEXTAREA_IFRAME = "//*[contains(@class,'cke_wysiwyg_frame')]";
    public static final String NAVIGATION_MENU_ITEM_CHECKED = "//parent::span[@aria-checked='true']";
    public static final String ACTIONS_SUB_MENU_ITEM = "//*[contains(@class,'leos-actions-sub-menu-item')]";
    public static final String RECENT_CHANGES_TEXT = "//*[text()='Recent changes']";
    public static final String CK_EDITABLE_INLINE = "//*[contains(@class,'cke_editable_inline')]";
    public static final String BILL = "//bill";
    public static final String ARTICLE = "//article";
    public static final String CITATIONS = "//citations";
    public static final String AKNP = "//aknp";
    public static final String AKNBODY = "//aknbody";
    public static final String RECITALS = "//recitals";
    public static final String CONTENT = "//content";
    public static final String LIST = "//li";
    public static final String HEADING = "//heading";
}

