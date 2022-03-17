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
package eu.europa.ec.leos.ui.component.milestones;

import com.google.common.eventbus.EventBus;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.components.grid.HeaderRow;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.ui.event.CreateRevisionRequestEvent;
import eu.europa.ec.leos.ui.event.FetchMilestoneEvent;
import eu.europa.ec.leos.ui.model.MilestonesVO;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.ui.component.actions.MilestoneActionMenu;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@SpringComponent
@ViewScope
public class MilestonesComponent extends CustomComponent {
    private static final long serialVersionUID = 8779532739907751262L;

    private TreeGrid<MilestonesVO> milestonesGrid;
    private MessageHelper messageHelper;
    private EventBus eventBus;
    private Boolean sendForRevisionEnabled;
    private Boolean isClonedProposal = false;

    enum COLUMN {
        TITLE("title"),
        DATE("date"),
        STATUS("status"),
        ACTION("action");

        private String key;
        private static final String[] keys = Stream.of(values()).map(COLUMN::getKey).toArray(String[]::new);

        COLUMN(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public static String[] getKeys() {
            return keys;
        }
    }

    @Autowired
    public MilestonesComponent(MessageHelper messageHelper, EventBus eventBus, ConfigurationHelper cfgHelper) {
        this.messageHelper = messageHelper;
        this.eventBus = eventBus;
        this.sendForRevisionEnabled = Boolean.valueOf(cfgHelper.getProperty("leos.sendForRevision.enabled"));
        initGrid();
    }

    public void populateData(Set<MilestonesVO> milestones) {
        this.getUI().access(() -> {
            milestonesGrid.setItems(milestones);
            milestones.forEach(milestone -> {
                List<MilestonesVO> childMilestone  = milestone.getClonedMilestones();
                if(childMilestone != null && !childMilestone.isEmpty()){
                    childMilestone.forEach(clonedMilestone -> milestonesGrid.getTreeData().addItem(milestone, clonedMilestone));
                }
                milestonesGrid.expand(milestone);
            });
            milestonesGrid.getDataProvider().refreshAll();
        });
        milestonesGrid.expand(milestones);
    }

    private void initGrid() {
        milestonesGrid = new TreeGrid<>();
        milestonesGrid.setSelectionMode(Grid.SelectionMode.NONE);

        HeaderRow mainHeader = milestonesGrid.getDefaultHeaderRow();

        if (sendForRevisionEnabled == null || !sendForRevisionEnabled) {
            Column<MilestonesVO, Button> titleColumn = milestonesGrid.addComponentColumn(vo -> {
                Button milestoneLink = new Button(vo.getTitle());
                milestoneLink.addStyleName("link");
                milestoneLink.addStyleName("milestone-exp-btn");
                milestoneLink.addClickListener(event -> {
                    eventBus.post(new FetchMilestoneEvent(vo.getLegDocumentName(), vo.getTitle()));
                });
                return milestoneLink;
            }).setDescriptionGenerator(MilestonesVO::getTitle);
            titleColumn.setMaximumWidth(250);
            mainHeader.getCell(titleColumn).setHtml(messageHelper.getMessage("milestones.header.column.title"));
        } else {
            Column<MilestonesVO, String> titleColumn = milestonesGrid.addColumn(MilestonesVO::getTitle).setDescriptionGenerator(MilestonesVO::getTitle);
            titleColumn.setMaximumWidth(250);
            mainHeader.getCell(titleColumn).setHtml(messageHelper.getMessage("milestones.header.column.title"));
        }
        Column<MilestonesVO, String> dateColumn = milestonesGrid.addColumn(MilestonesVO::getCreatedDate).setDescriptionGenerator(MilestonesVO::getCreatedDate);
        Column<MilestonesVO, String> statusColumn = milestonesGrid.addColumn(MilestonesVO::getStatus).setDescriptionGenerator(MilestonesVO::getStatus);

        dateColumn.setMaximumWidth(160);
        statusColumn.setMaximumWidth(150);

        createMilestoneActionMenu();

        mainHeader.getCell(dateColumn).setHtml(messageHelper.getMessage("milestones.header.column.date"));
        mainHeader.getCell(statusColumn).setHtml(messageHelper.getMessage("milestones.header.column.status"));

        milestonesGrid.setHeightMode(HeightMode.ROW);
        milestonesGrid.setWidth(100, Unit.PERCENTAGE);
        setCompositionRoot(milestonesGrid);
    }

    private void createMilestoneActionMenu() {
        if(sendForRevisionEnabled != null && sendForRevisionEnabled){
            Grid.Column<MilestonesVO, MilestoneActionMenu> milestoneActionsColumn = milestonesGrid.addComponentColumn(vo -> {
                if(vo.isClone() == null || !vo.isClone()){
                MilestoneActionMenu actionMenuItem = new MilestoneActionMenu(messageHelper, eventBus);
                actionMenuItem.createMenuItem(messageHelper.getMessage("legal.service.menu.item.view"), selectedItem -> viewMilestoneExplorer(vo));
                if(!isClonedProposal) {
                    actionMenuItem.createMenuItem(messageHelper.getMessage("legal.service.menu.item.send.revision"), selectedItem -> sendCopyForRevision(vo));
                }
                actionMenuItem.addStyleName("leos-actions-milestone-menu");
                return actionMenuItem;
                }else {
                    return null;
                }
            });
            milestoneActionsColumn.setMaximumWidth(100);
        }
    }

    public void setClonedProposal(Boolean clonedProposal) {
        isClonedProposal = clonedProposal;
    }

    private void sendCopyForRevision(MilestonesVO vo) {
        eventBus.post(new CreateRevisionRequestEvent(vo));
    }

    private void viewMilestoneExplorer(MilestonesVO vo) {
        eventBus.post(new FetchMilestoneEvent(vo.getLegDocumentName(), vo.getTitle()));
    }
}
