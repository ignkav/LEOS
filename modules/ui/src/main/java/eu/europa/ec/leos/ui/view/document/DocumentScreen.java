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
package eu.europa.ec.leos.ui.view.document;

import com.vaadin.server.StreamResource;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.ui.view.ComparisonDisplayMode;
import eu.europa.ec.leos.ui.view.TriFunction;
import eu.europa.ec.leos.vo.coedition.CoEditionVO;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.web.event.view.document.CheckElementCoEditionEvent.Action;
import eu.europa.ec.leos.web.model.VersionInfoVO;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

interface DocumentScreen {

    void setDocumentTitle(final String documentTitle);

    void setDocumentVersionInfo(VersionInfoVO versionInfoVO);

    void refreshContent(final String documentContent);

    void populateMarkedContent(String comparedContent, String comparedInfo, Bill original, Bill current);

    void populateDoubleComparisonContent(String comparedContent, String comparedInfo, Bill original, Bill intermediate, Bill current);

    void setToc(List<TableOfContentItemVO> tableOfContentItemVoList);
    
    void showElementEditor(String elementId, String elementTagName, String elementContent, String alternatives);

    void refreshElementEditor(String elementId, String elementTagName, String elementContent);
    
    void enableTocEdition(List<TableOfContentItemVO> tableOfContentItemVoList);

    void showTimeLineWindow(List documentVersions);

    void updateTimeLineWindow(List documentVersions);

    void showIntermediateVersionWindow();

    void showImportWindow();

    void displayComparison(HashMap<ComparisonDisplayMode, Object> htmlCompareResult);

    void setTocAndAncestors(Map<String, List<TableOfContentItemVO>> tocItemList, List<String> elementAncestorsIds);

    void setElement(String elementId, String elementTagName, String elementContent, String documentRef);

    void setUserGuidance(String jsonGuidance);

    void sendUserPermissions(List<LeosPermission> userPermissions);

    void displaySearchedContent(String content);

    void closeImportWindow();

    void setPermissions(DocumentVO bill, boolean isClonedProposal);

    void initAnnotations(DocumentVO bill, String proposalRef, String connectedEntity);

    void scrollToMarkedChange(final String elementId);

    void scrollTo(final String elementId);

    void setReferenceLabel(String referenceLabels, String documentRef);

    void updateUserCoEditionInfo(List<CoEditionVO> coEditionVos, String presenterId);

    void displayDocumentUpdatedByCoEditorWarning();

    void checkElementCoEdition(List<CoEditionVO> coEditionVos, User user, final String elementId, final String elementTagName, final Action action, final Object actionEvent);

    void showAlertDialog(String messageKey);

    boolean isTocEnabled();
    
    void setDataFunctions(List<VersionVO> allVersions,
                          BiFunction<Integer, Integer, List<Bill>> majorVersionsFn,
                          Supplier<Integer> countMajorVersionsFn,
                          TriFunction<String, Integer, Integer, List<Bill>> minorVersionsFn,
                          Function<String, Integer> countMinorVersionsFn,
                          BiFunction<Integer, Integer, List<Bill>> recentChangesFn,
                          Supplier<Integer> countRecentChangesFn);
    
    void refreshVersions(List<VersionVO> allVersions, boolean isComparisonMode);
    
    void showVersion(String content, String versionInfo);

    void showMilestoneExplorer(LegDocument legDocument, String milestoneTitle, String proposalRef);
    
    void cleanComparedContent();

    boolean isComparisonComponentVisible();
    
    void setDownloadStreamResourceForExport(StreamResource streamResource);
    
    void setDownloadStreamResourceForMenu(DownloadStreamResource streamResource);
    
    void setDownloadStreamResourceForVersion(StreamResource streamResource, String documentId);
    
    void setDownloadStreamResourceForXmlFiles(StreamResource streamResource);

    void populateCloneProposalMetadataVO(CloneProposalMetadataVO cloneProposalMetadataVO);

    /** to be called after search to set the matching results */
    void showMatchResults(Long searchId, List<SearchMatchVO> results);

    /** to be called after replace is done
     * TODO: To be reflected in Match. But no save done. What should be sent to UI??
     * */
    void matchReplaced(List<SearchMatchVO>  result);

    /**
     * To be called once a single replace is done, to make sure ui is updated
     * @param searchId
     * @param matchedIndex
     * @param isReplaced
     */
    void refineSearch(Long searchId, int matchedIndex, boolean isReplaced);


    /** to be called to close the window after cleanup is done on presenter side
     * TODO: Should we prompt for save before closing the bar?? */
    void closeSearchBar();

    /** reflect save on ui. I think search bar should be closed on save
     * setMatchResults should be called after save is done to show remaining items
     * */
    void saveDoneForReplacedItems();
}
