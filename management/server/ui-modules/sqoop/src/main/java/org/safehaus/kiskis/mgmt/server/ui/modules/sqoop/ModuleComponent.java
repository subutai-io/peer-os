package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command.CommandBuilder;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.command.CommandExecutor;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ChainManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UILogger;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view.UIStateManager;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

public class ModuleComponent extends CustomComponent implements CommandListener {

    private final String moduleName;

    public ModuleComponent(String moduleName) {

        this.moduleName = moduleName;

        setHeight("100%");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();

        /*
        TabSheet commandsSheet;

        commandsSheet = new TabSheet();
        commandsSheet.setStyleName(Runo.TABSHEET_SMALL);
        commandsSheet.setSizeFull();
        */

/*        manager = new Manager(taskRunner);
        cloner = new Cloner(commandsSheet, taskRunner, manager);
        commandsSheet.addTab(cloner, "Clone");
        commandsSheet.addTab(manager, "Manage");*/

        VerticalLayout verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayout.setSpacing(true);

        final TextField textFieldClusterName = new TextField("Enter your cluster name");
        textFieldClusterName.setInputPrompt("Cluster name");
        textFieldClusterName.setRequired(true);
        textFieldClusterName.setRequiredError("Must have a name");
        verticalLayoutForm.addComponent(textFieldClusterName);
        verticalLayoutForm.setSpacing(true);

        final TextField textFieldDomainName = new TextField("Enter your domain name");
        textFieldDomainName.setInputPrompt("intra.lan");
        textFieldDomainName.setRequired(true);
        textFieldDomainName.setRequiredError("Must have a name");
        verticalLayoutForm.addComponent(textFieldDomainName);

        GridLayout grid = getGrid();
        grid.addComponent(verticalLayoutForm, 0, 0, 5, 0);
        //setCompositionRoot(grid);

        //grid.setComponentAlignment(topleft, Alignment.TOP_LEFT);

        /*
        Button topcenter = new Button("Top Center");
        grid.addComponent(topcenter, 1, 0);
        grid.setComponentAlignment(topcenter, Alignment.TOP_CENTER);

        Button topright = new Button("Top Right");
        grid.addComponent(topright, 2, 0);
        grid.setComponentAlignment(topright, Alignment.TOP_RIGHT);
        */

        TextArea textArea = getTextArea();
        grid.addComponent(textArea, 0, 1, 9, 9);


        VerticalLayout layout = new VerticalLayout(); // 100% default width
        Label label = new Label("Hello"); // 100% default width
        label.setSizeUndefined();
        layout.addComponent(label);
        layout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

        //commandsSheet.addTab(verticalLayoutForm, "Manage");


        AbsoluteLayout alayout = new AbsoluteLayout();
        layout.setWidth("400px");
        layout.setHeight("250px");

        // At the top-left corner
        Button button = new Button( "left: 10px; top: 10px;");
        alayout.addComponent(button, "left: 0px; top: 0px;");

        button.setWidth(120, Sizeable.UNITS_PIXELS);
        //grid.setHeight(200, Sizeable.UNITS_PIXELS);

// At the bottom-right corner
        Button buttCorner = new Button( "right: 0px; bottom: 0px;");
        alayout.addComponent(buttCorner, "right: 0px; bottom: 0px;");

// Relative to the bottom-right corner
        Button buttBrRelative = new Button( "right: 50px; bottom: 50px;");
        alayout.addComponent(buttBrRelative, "right: 50px; bottom: 50px;");

// On the bottom, relative to the left side
        Button buttBottom = new Button( "left: 50px; bottom: 0px;");
        alayout.addComponent(buttBottom, "left: 50px; bottom: 0px;");

// On the right side, up from the bottom
        Button buttRight = new Button( "right: 0px; bottom: 100px;");
        alayout.addComponent(buttRight, "right: 0px; bottom: 100px;");

        TabSheet commandsSheet;

        commandsSheet = new TabSheet();
        commandsSheet.setStyleName(Runo.TABSHEET_SMALL);
        commandsSheet.setSizeFull();

        commandsSheet.addTab(alayout, "Manage");

        //grid.setComponentAlignment(verticalLayoutForm, Alignment.TOP_CENTER);

        setCompositionRoot(commandsSheet);

        //verticalLayout.addComponent(commandsSheet);
        //setCompositionRoot(verticalLayout);

        /*
        CommandBuilder.init(moduleName);

        setHeight("100%");

        GridLayout grid = getGrid();
        setCompositionRoot(grid);

        TextArea textArea = getTextArea();
        grid.addComponent(textArea, 1, 0, 9, 9);
        UILogger.init(textArea);

        addButtons(grid);
        */
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
        //grid.setSizeFull();
        grid.setMargin(true);
        grid.setSpacing(true);

        grid.setWidth(400, Sizeable.UNITS_PIXELS);
        grid.setHeight(200, Sizeable.UNITS_PIXELS);

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