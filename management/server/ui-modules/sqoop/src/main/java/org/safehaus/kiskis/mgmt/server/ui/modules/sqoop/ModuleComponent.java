package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command.CommandBuilder;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command.CommandExecutor;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.ManageLayoutBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

public class ModuleComponent extends CustomComponent implements CommandListener {

    private final String moduleName;

    public ModuleComponent(String moduleName) {

        this.moduleName = moduleName;
        CommandBuilder.setSource(moduleName);

        setHeight("100%");
        setCompositionRoot(getTabSheet());
    }

    private static TabSheet getTabSheet() {

        TabSheet manageTab = new TabSheet();
        manageTab.setSizeFull();
        manageTab.addTab(ManageLayoutBuilder.get(), "Manage");

        return manageTab;
    }

    @Override
    public void onCommand(Response response) {
        CommandExecutor.INSTANCE.onResponse(response);
    }

    @Override
    public String getName() {
        return moduleName;
    }

}