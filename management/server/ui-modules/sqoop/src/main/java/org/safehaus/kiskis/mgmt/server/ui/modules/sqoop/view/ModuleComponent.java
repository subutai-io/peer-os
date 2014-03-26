package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command.CommandBuilder;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.imp.ImportLayout;

public class ModuleComponent extends CustomComponent {

    private final String moduleName;

    public ModuleComponent(String moduleName) {

        this.moduleName = moduleName;
        CommandBuilder.setSource(moduleName);

        setHeight("100%");
        setCompositionRoot( getTabSheet() );
    }

    private TabSheet getTabSheet() {

        TabSheet manageTab = new TabSheet();
        manageTab.setSizeFull();

        manageTab.addTab(ManageLayoutBuilder.create(), "Manage");
        manageTab.addTab(new ImportLayout(), "Import");
        manageTab.addTab(ExportLayoutBuilder.create(), "Export");

        return manageTab;
    }

}
