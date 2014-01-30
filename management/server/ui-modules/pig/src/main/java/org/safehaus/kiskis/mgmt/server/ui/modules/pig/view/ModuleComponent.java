package org.safehaus.kiskis.mgmt.server.ui.modules.pig.view;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.Pig;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.action.Action;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.command.CommandBuilder;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.command.CommandHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.Set;
import java.util.logging.Logger;

public class ModuleComponent extends CustomComponent implements CommandListener {

    private static final Logger LOG = Logger.getLogger(ModuleComponent.class.getName());
    private final TextArea commandOutputTxtArea;

    public ModuleComponent() {

        LOG.info("Creating ModuleComponent");

        setHeight("100%");
        GridLayout grid = new GridLayout(10, 10);
        grid.setSizeFull();
        grid.setMargin(true);
        grid.setSpacing(true);

        setCompositionRoot(grid);

        commandOutputTxtArea = new TextArea("Log:");
        commandOutputTxtArea.setSizeFull();
        commandOutputTxtArea.setImmediate(true);
        commandOutputTxtArea.setWordwrap(false);
        grid.addComponent(commandOutputTxtArea, 1, 0, 9, 9);


        Button statusButton = new Button("Update Status");
        grid.addComponent(statusButton, 0, 0);

        statusButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                AgentManager agentManager = ServiceLocator.getService(AgentManager.class);
                Set<Agent> agents = MgmtApplication.getSelectedAgents();
                Command cmd = null;

                for (Agent agent : agents) {

                    LOG.info(">> agent id: " + agent.getUuid());
                    LOG.info(">> hostname: " + agent.getHostname());

                    cmd = CommandBuilder.getTemplate();
                    cmd.getRequest().setUuid(agent.getUuid());
                    //cmd.getRequest().setProgram("dpkg -l|grep ksks-hadoop");
                    cmd.getRequest().setProgram("dpkg -l");
                    cmd.getRequest().setTimeout(30);
                    cmd.getRequest().setWorkingDirectory("/");
                }

                CommandHandler.handle(cmd, new Action());
            }
        });



        Button clearButton = new Button("Install");
        grid.addComponent(clearButton, 0, 1);

        clearButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {




            }
        });

        Button removeButton = new Button("Remove");
        grid.addComponent(removeButton, 0, 2);

        removeButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                alert("remove");
                LOG.info("remove");
                commandOutputTxtArea.setValue("remove");
            }
        });
    }

    private void alert(String message) {
        getWindow().showNotification(message);
    }

    @Override
    public void onCommand(Response response) {
        CommandHandler.INSTANCE.onCommand(response);
    }

    @Override
    public String getName() {
        return Pig.MODULE_NAME;
    }

}