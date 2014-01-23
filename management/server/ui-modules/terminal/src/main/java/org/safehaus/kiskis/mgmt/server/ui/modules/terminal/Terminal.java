package org.safehaus.kiskis.mgmt.server.ui.modules.terminal;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Logger;

public class Terminal implements Module {

    public static final String MODULE_NAME = "Terminal";
    private static final Logger LOG = Logger.getLogger(Terminal.class.getName());

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final TextArea commandOutputTxtArea;
        private final TextField programTxtFld;
        private final CommandManager commandManagerInterface;
        private final AgentManager agentManager;
        private final TaskRunner taskRunner = new TaskRunner();

        public ModuleComponent() {
            commandManagerInterface = ServiceLocator.getService(CommandManager.class);
            agentManager = ServiceLocator.getService(AgentManager.class);

            VerticalLayout content = new VerticalLayout();
            content.setSpacing(true);
            content.setMargin(true);

            commandOutputTxtArea = new TextArea("Commands output");
            commandOutputTxtArea.setRows(40);
            commandOutputTxtArea.setColumns(80);
            commandOutputTxtArea.setImmediate(true);
            commandOutputTxtArea.setWordwrap(false);
            content.addComponent(commandOutputTxtArea);

            GridLayout grid = new GridLayout(10, 2);
            grid.setSizeFull();
//            grid.setSpacing(true);
//            grid.setMargin(true);
            Label programLbl = new Label("Program");
            programTxtFld = new TextField();
            programTxtFld.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            programTxtFld.setValue("ls");
            grid.addComponent(programLbl, 0, 0, 0, 0);
            grid.addComponent(programTxtFld, 1, 0, 4, 0);
            Label workDirLbl = new Label("WorkDir");
            TextField workDirTxtFld = new TextField();
             workDirTxtFld.setWidth(100, Sizeable.UNITS_PERCENTAGE);
             workDirTxtFld.setValue("ls");            
            Button sendBtn = new Button("Send");
            grid.addComponent(sendBtn, 9, 0, 9, 0);

            content.addComponent(grid);

            setCompositionRoot(content);

        }

        @Override
        public void onCommand(Response response) {

        }

        @Override
        public String getName() {
            return Terminal.MODULE_NAME;
        }

    }

    @Override
    public String getName() {
        return Terminal.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent();
    }

}
