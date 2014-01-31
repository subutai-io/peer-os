package org.safehaus.kiskis.mgmt.server.ui.modules.pig.view;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.Pig;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.command.ActionListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.command.CommandExecutor;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.*;
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

                Set<Agent> agents = MgmtApplication.getSelectedAgents();
                Agent agent = null;

                for (Agent a : agents) {
                    agent = a;
                }

                Context context = new Context();
                context.put("agent", agent);

                ActionListener actionListener = new ActionListener() {

                    @Override
                    public void onExecute(Context context) {
                        LOG.info("on execute");
                    }

                    @Override
                    public void onResponse(Context context, String stdOut, String stdErr, boolean isError) {
                        LOG.info("on response");
                    }
                };

                CommandAction commandAction = new CommandAction("dpkg -l|grep ksks", actionListener);

                Chain chain = new Chain(commandAction);
                chain.execute(context);

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
        CommandExecutor.INSTANCE.onResponse(response);
    }

    @Override
    public String getName() {
        return Pig.MODULE_NAME;
    }

}