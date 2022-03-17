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
package eu.europa.ec.leos.ui.view.explanatory;

import com.vaadin.server.StreamResource;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

interface ExplanatoryScreen {

    void setTitle(String title);

    void setContent(String content);

    void refreshElementEditor(String elementId, String elementTagName, String elementContent);

    void showElementEditor(String elementId, String elementTagName, String element);

    void populateComparisonContent(String comparedContent, String comparedInfo, Explanatory original, Explanatory current);

    void populateDoubleComparisonContent(String comparedContent, String comparedInfo, Explanatory original, Explanatory intermediate, Explanatory current);

    void showTimeLineWindow(List<Explanatory> documentVersions);

    void updateTimeLineWindow(List<Explanatory> documentVersions);

    void displayComparison(HashMap<ComparisonDisplayMode, Object> htmlCompareResult);

    void showIntermediateVersionWindow();

    void setDocumentVersionInfo(VersionInfoVO versionInfoVO);

    void setToc(List<TableOfContentItemVO> tableOfContentItemVoList);
    
    void setPermissions(DocumentVO explanatory);

    void initAnnotations(DocumentVO explanatory, String proposalRef, String connectedEntity);

    void scrollToMarkedChange(final String elementId);

    void sendUserPermissions(List<LeosPermission> userPermissions);

    void updateUserCoEditionInfo(List<CoEditionVO> coEditionVos, String presenterId);

    void displayDocumentUpdatedByCoEditorWarning();

    void checkElementCoEdition(List<CoEditionVO> coEditionVos, User user, final String elementId, final String elementTagName, final Action action,
                               final Object actionEvent);

    void showAlertDialog(String messageKey);

    void enableTocEdition(List<TableOfContentItemVO> tableOfContent);

    boolean isTocEnabled();
    
    void setDataFunctions(List<VersionVO> allVersions,
                          BiFunction<Integer, Integer, List<Explanatory>> majorVersionsFn,
                          Supplier<Integer> countMajorVersionsFn,
                          TriFunction<String, Integer, Integer, List<Explanatory>> minorVersionsFn,
                          Function<String, Integer> countMinorVersionsFn,
                          BiFunction<Integer, Integer, List<Explanatory>> recentChangesFn,
                          Supplier<Integer> countRecentChangesFn);
    
    void refreshVersions(List<VersionVO> allVersions, boolean isComparisonMode);
    
    void showVersion(String versionContent, String versionInfo);
    
    void cleanComparedContent();

    boolean isComparisonComponentVisible();
    
    void showMilestoneExplorer(LegDocument legDocument, String milestoneTitle, String proposalRef);
    
    void scrollTo(String elementId);
    
    void setDownloadStreamResourceForExport(StreamResource streamResource);
    
    void setDownloadStreamResourceForMenu(DownloadStreamResource streamResource);
    
    void setDownloadStreamResourceForVersion(StreamResource streamResource, String documentId);
    
    void setDownloadStreamResourceForXmlFiles(StreamResource streamResource);

    /** to be called after search to set the matching results */
    void showMatchResults(Long searchId, List<SearchMatchVO> results);

    /** to be called to close the window after cleanup is done on presenter side*/
    void closeSearchBar();

    void refineSearch(Long searchId, int matchedIndex, boolean isReplaced);

    void populateCloneProposalMetadataVO(CloneProposalMetadataVO cloneProposalMetadataVO);
}
