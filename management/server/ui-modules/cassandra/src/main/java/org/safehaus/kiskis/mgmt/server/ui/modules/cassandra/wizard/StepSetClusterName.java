/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands.CassandraCommands;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class StepSetClusterName extends Panel {

    private Task task;

    public StepSetClusterName(final CassandraWizard wizard) {

        GridLayout gridLayout = new GridLayout(10, 6);
        gridLayout.setSizeFull();

        Label welcomeMsg = new Label(
                "<center><h2>Welcome to Cassandra Installation Wizard!</h2><br/>"
                + "Please select nodes in the tree on the left to continue</center>");
        welcomeMsg.setContentMode(Label.CONTENT_XHTML);
        gridLayout.addComponent(welcomeMsg, 3, 1, 6, 2);

        Label logoImg = new Label("<img src='http://localhost:" + Common.WEB_SERVER_PORT + "/cassandra-logo.png' width='150px'/>");
        logoImg.setContentMode(Label.CONTENT_XHTML);
        gridLayout.addComponent(logoImg, 1, 3, 2, 5);

        Button next = new Button("Next");
        next.setWidth(100, Sizeable.UNITS_PIXELS);
        gridLayout.addComponent(next, 6, 4, 6, 4);
        gridLayout.setComponentAlignment(next, Alignment.BOTTOM_RIGHT);

        next.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Set<Agent> selectedAgents = MgmtApplication.getSelectedAgents();
                if (Util.isCollectionEmpty(selectedAgents)) {
                    show("Select nodes in the tree on the left first");
                } else {
                    wizard.getConfig().setSelectedAgents(selectedAgents);
                    task = RequestUtil.createTask(CassandraWizard.getCommandManager(), "Intall Cassandra");
                    for (Agent agent : wizard.getConfig().getSelectedAgents()) {
                        
                        Command command  = CassandraCommands.getSetClusterNameCommand();
                        command.getRequest().setUuid(agent.getUuid());
                        command.getRequest().setSource(CassandraWizard.SOURCE);
                        command.getRequest().setUuid(agent.getUuid());
                        command.getRequest().setTaskUuid(task.getUuid());
                        command.getRequest().setRequestSequenceNumber(task.getIncrementedReqSeqNumber());
                        
                        CassandraWizard.getCommandManager().executeCommand(command);
                    }
                    wizard.next();
                }
            }
        });

        addComponent(gridLayout);
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }
    
    

}
