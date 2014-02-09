package org.safehaus.kiskis.mgmt.server.ui.modules.pig;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.CommandBuilder;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.CommandExecutor;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.action.ChainManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.view.UIStateManager;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

public class ModuleComponent extends CustomComponent implements CommandListener {

    private final String moduleName;

    public ModuleComponent(String moduleName) {

        this.moduleName = moduleName;
        CommandBuilder.init(moduleName);

        setHeight("100%");

        GridLayout grid = getGrid();
        setCompositionRoot(grid);

        TextArea textArea = getTextArea();
        grid.addComponent(textArea, 1, 0, 9, 9);
        UILogger.init(textArea);

        addButtons(grid);
    }

    private static void addButtons(GridLayout grid) {

        Button statusButton = getButton("Check Status", ChainManager.STATUS_CHAIN);
        Button installButton = getButton("Install", ChainManager.INSTALL_CHAIN);
        Button removeButton = getButton("Remove", ChainManager.REMOVE_CHAIN);

        grid.addComponent(statusButton, 0, 0);
        grid.addComponent(installButton, 0, 1);
        grid.addComponent(removeButton, 0, 2);

        UIStateManager.init(statusButton, installButton, removeButton);
    }

    private static Button getButton(String name, final Chain chain) {

        Button button = new Button(name);

        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ChainManager.run(chain);
            }
        });

        return button;
    }

    private static TextArea getTextArea() {

        TextArea textArea = new TextArea("Log:");
        textArea.setSizeFull();
        textArea.setImmediate(true);
        textArea.setWordwrap(false);

        return textArea;
    }

    private static GridLayout getGrid() {

        GridLayout grid = new GridLayout(10, 10);
        grid.setSizeFull();
        grid.setMargin(true);
        grid.setSpacing(true);

        return grid;
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