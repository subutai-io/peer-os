package org.safehaus.kiskis.mgmt.server.ui.modules.pig.view;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.Pig;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.CommandExecutor;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.*;
import java.util.logging.Logger;

public class ModuleComponent extends CustomComponent implements CommandListener {

    private static final Logger LOG = Logger.getLogger(ModuleComponent.class.getName());
    private final TextArea logTextArea;

    public ModuleComponent() {

        LOG.info("Creating ModuleComponent");

        setHeight("100%");
        GridLayout grid = new GridLayout(10, 10);
        grid.setSizeFull();
        grid.setMargin(true);
        grid.setSpacing(true);

        setCompositionRoot(grid);

        logTextArea = new TextArea("Log:");
        logTextArea.setSizeFull();
        logTextArea.setImmediate(true);
        logTextArea.setWordwrap(false);
        grid.addComponent(logTextArea, 1, 0, 9, 9);

        UILog uiLog = new UILog(logTextArea);

        grid.addComponent(StatusButtonManager.getButton(uiLog), 0, 0);
        grid.addComponent(InstallButtonManager.getButton(uiLog), 0, 1);
        grid.addComponent(RemoveButtonManager.getButton(uiLog), 0, 2);

    }

    private static void log(String message) {

    }

    @Override
    public void onCommand(Response response) {
        CommandExecutor.INSTANCE.onResponse(response);
    }

    @Override
    public String getName() {
        return Pig.MODULE_NAME;
    }

}