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
package eu.europa.ec.leos.ui.component.export;

import java.util.Set;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.VerticalLayout;
import eu.europa.ec.leos.ui.event.UpdateCommentsExportPackageEvent;
import eu.europa.ec.leos.ui.event.DownloadExportPackageEvent;
import eu.europa.ec.leos.web.ui.component.EditBoxComponent;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.dialogs.ConfirmDialog;
import com.google.common.eventbus.EventBus;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.grid.HeaderRow;

import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.ui.event.DeleteExportPackageEvent;
import eu.europa.ec.leos.ui.event.NotifyExportPackageEvent;
import eu.europa.ec.leos.ui.model.ExportPackageVO;
import eu.europa.ec.leos.web.ui.component.actions.ExportPackageActionsMenu;
import org.vaadin.simplefiledownloader.SimpleFileDownloader;

@SpringComponent
@ViewScope
public class ExportPackageComponent extends CustomComponent {

    private static final long serialVersionUID = -7967788237436911888L;
    
    private Grid<ExportPackageVO> exportPackageGrid;
    private MessageHelper messageHelper;
    private EventBus eventBus;
    private SimpleFileDownloader fileDownloader;

    @Autowired
    public ExportPackageComponent(MessageHelper messageHelper, EventBus eventBus) {
        this.messageHelper = messageHelper;
        this.eventBus = eventBus;
        initGrid();
        initDownloader();
    }

    private void initDownloader() {
        fileDownloader = new SimpleFileDownloader();
        addExtension(fileDownloader);
    }

    public void setDownloadStreamResource(StreamResource downloadResource) {
        fileDownloader.setFileDownloadResource(downloadResource);
        fileDownloader.download();
    }

    public void populateData(Set<ExportPackageVO> exportPackages) {
        this.getUI().access(() -> {
            exportPackageGrid.setItems(exportPackages);
            exportPackageGrid.getDataProvider().refreshAll();
        });
    }

    private void initGrid() {
        exportPackageGrid = new Grid<>();
        exportPackageGrid.setSelectionMode(Grid.SelectionMode.NONE);
        exportPackageGrid.setStyleName("export-package-grid");

        Grid.Column<ExportPackageVO, VerticalLayout> titleColumn = exportPackageGrid.addComponentColumn(vo -> {
            EditBoxComponent titleEditable = new EditBoxComponent(true);
            titleEditable.setPlaceholder(messageHelper.getMessage("collection.block.export.package.column.title.prompt"));
            titleEditable.setRequired(messageHelper.getMessage("collection.block.export.package.column.title.error"));
            titleEditable.setValue(vo.getMainComment());
            titleEditable.setSizeFull();
            titleEditable.setDescription(vo.getMainComment());
            titleEditable.addValueChangeListener(event -> {
                vo.setMainComment(event.getValue());
                eventBus.post(new UpdateCommentsExportPackageEvent(vo.getId(), vo.getComments()));
            });
            TextField titleComment1 = new TextField();
            titleComment1.setValue(vo.getComment(1));
            titleComment1.setReadOnly(true);
            titleComment1.setSizeFull();
            titleComment1.setDescription(vo.getComment(1));
            titleComment1.setStyleName("export-package-first-comment");
            TextField titleComment2 = new TextField();
            titleComment2.setValue(vo.getComment(2));
            titleComment2.setReadOnly(true);
            titleComment2.setSizeFull();
            titleComment2.setDescription(vo.getComment(2));
            titleComment2.setStyleName("export-package-second-comment");
            VerticalLayout titleVerticalLayout = new VerticalLayout(titleEditable, titleComment1, titleComment2);
            titleVerticalLayout.setSpacing(false);
            titleVerticalLayout.setSizeFull();
            titleVerticalLayout.setStyleName("export-package-comments");
            return titleVerticalLayout;
        });
        titleColumn.setExpandRatio(1);

        Grid.Column<ExportPackageVO, String> dateColumn = exportPackageGrid.addColumn(ExportPackageVO::getDateFormatted);
        dateColumn.setWidth(120);

        Grid.Column<ExportPackageVO, String> statusColumn = exportPackageGrid.addColumn(ExportPackageVO::getStatus);
        statusColumn.setWidth(90);

        Grid.Column<ExportPackageVO, ExportPackageActionsMenu> actionColumn = exportPackageGrid.addComponentColumn(vo -> {
            ExportPackageActionsMenu exportPackageActionsMenu = new ExportPackageActionsMenu(messageHelper, eventBus);
            exportPackageActionsMenu.createMenuItem(messageHelper.getMessage("collection.block.export.package.action.notify"), selectedItem -> notifyExportPackage(vo));
            exportPackageActionsMenu.createMenuItem(messageHelper.getMessage("collection.block.export.package.action.download"), selectedItem -> downloadExportPackage(vo));
            exportPackageActionsMenu.createMenuItem(messageHelper.getMessage("collection.block.export.package.action.delete"), selectedItem -> deleteExportPackage(vo));
            exportPackageActionsMenu.addStyleName("leos-actions-export-package-menu");
            return exportPackageActionsMenu;
        });
        actionColumn.setWidth(40);

        HeaderRow mainHeader = exportPackageGrid.getDefaultHeaderRow();
        mainHeader.getCell(titleColumn).setHtml(messageHelper.getMessage("collection.block.export.package.header.column.title"));
        mainHeader.getCell(dateColumn).setHtml(messageHelper.getMessage("collection.block.export.package.header.column.date"));
        mainHeader.getCell(statusColumn).setHtml(messageHelper.getMessage("collection.block.export.package.header.column.status"));

        exportPackageGrid.setHeightMode(HeightMode.ROW);
        exportPackageGrid.setWidth(100, Unit.PERCENTAGE);
        exportPackageGrid.setHeight(200, Unit.PIXELS);
        setCompositionRoot(exportPackageGrid);
    }

    private void deleteExportPackage(ExportPackageVO exportPackage) {
        ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                messageHelper.getMessage("collection.block.export.package.action.confirmation.delete.title"),
                messageHelper.getMessage("collection.block.export.package.action.confirmation.delete.message"),
                messageHelper.getMessage("collection.block.export.package.action.confirmation.confirm"),
                messageHelper.getMessage("collection.block.export.package.action.confirmation.cancel"), null);
        confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
        confirmDialog.getContent().setHeightUndefined();
        confirmDialog.setHeightUndefined();
        confirmDialog.show(getUI(), dialog -> {
            if (dialog.isConfirmed()) {
                eventBus.post(new DeleteExportPackageEvent(exportPackage.getId(), exportPackage.getVersionId()));
            }
        }, true);
    }

    private void notifyExportPackage(ExportPackageVO exportPackage) {
        eventBus.post(new NotifyExportPackageEvent(exportPackage.getId()));
    }

    private void downloadExportPackage(ExportPackageVO exportPackage) {
        eventBus.post(new DownloadExportPackageEvent(exportPackage.getId()));
    }
}