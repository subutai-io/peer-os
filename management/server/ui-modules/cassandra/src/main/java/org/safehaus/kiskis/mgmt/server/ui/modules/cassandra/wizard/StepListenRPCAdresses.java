/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.commands.CassandraCommands;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class StepListenRPCAdresses extends Panel {

    private Task task;

    public StepListenRPCAdresses(final CassandraWizard wizard) {

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(10, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Installation Wizard<br>"
                + " 1) <font color=\"#f14c1a\"><strong>Set listen and rpc addresses</strong></font><br>"
                + " 2) Replica Set Configurations");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 2, 1);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        VerticalLayout verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayoutForm.setSpacing(true);

        grid.addComponent(verticalLayoutForm, 3, 0, 9, 9);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                task = RequestUtil.createTask(CassandraWizard.getCommandManager(), "Set lister and rpc addresses");
                for (Agent agent : wizard.getConfig().getSelectedAgents()) {

                    Command listenAddressCommand = CassandraCommands.getSetListenAddressCommand();
                    listenAddressCommand.getRequest().setUuid(agent.getUuid());
                    listenAddressCommand.getRequest().setSource(CassandraWizard.SOURCE);
                    listenAddressCommand.getRequest().setUuid(agent.getUuid());
                    listenAddressCommand.getRequest().setTaskUuid(task.getUuid());
                    listenAddressCommand.getRequest().setRequestSequenceNumber(task.getIncrementedReqSeqNumber());

                    Command rpcAddressCommand = CassandraCommands.getSetListenAddressCommand();
                    rpcAddressCommand.getRequest().setUuid(agent.getUuid());
                    rpcAddressCommand.getRequest().setSource(CassandraWizard.SOURCE);
                    rpcAddressCommand.getRequest().setUuid(agent.getUuid());
                    rpcAddressCommand.getRequest().setTaskUuid(task.getUuid());
                    rpcAddressCommand.getRequest().setRequestSequenceNumber(task.getIncrementedReqSeqNumber());
//                    CassandraWizard.getCommandManager().executeCommand(listenAddressCommand);
//                    CassandraWizard.getCommandManager().executeCommand(rpcAddressCommand);
                }
                wizard.next();
            }
        });

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });

        verticalLayout.addComponent(grid);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
