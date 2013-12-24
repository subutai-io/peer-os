/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.data.Property;
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
import java.util.List;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class Step3 extends Panel {

    public Step3(final MongoWizard mongoWizard) {

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(10, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Installation Wizard<br>"
                + " 1) <strong>Config Servers and Routers<br>"
                + " 2) <font color=\"#f14c1a\">Replica Set Configurations</strong></font>");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 2, 1);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        VerticalLayout verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayoutForm.setSpacing(true);

        final TextField replicaNameTxtFld = new TextField("Enter Replica Set name");
        replicaNameTxtFld.setInputPrompt("Replica Set name");
        replicaNameTxtFld.setRequired(true);
        replicaNameTxtFld.setMaxLength(10);
        replicaNameTxtFld.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                mongoWizard.getConfig().setReplicaSetName(event.getProperty().getValue().toString().trim());
            }
        });
        verticalLayoutForm.addComponent(replicaNameTxtFld);

        Label configServersLabel = new Label("<strong>Choose hosts that will act as shards<br>"
                + "(Recommended odd number of servers, provide at least 1)</strong>");
        configServersLabel.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(configServersLabel);

        TwinColSelect shardsColSel = new TwinColSelect("", new ArrayList<Agent>());
        shardsColSel.setItemCaptionPropertyId("hostname");
        shardsColSel.setRows(7);
        shardsColSel.setNullSelectionAllowed(true);
        shardsColSel.setMultiSelect(true);
        shardsColSel.setImmediate(true);
        shardsColSel.setRequired(true);
        shardsColSel.setLeftColumnCaption("Available Nodes");
        shardsColSel.setRightColumnCaption("Shards");
        shardsColSel.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        shardsColSel.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Set<Agent> agentList = (Set<Agent>) event.getProperty().getValue();
                mongoWizard.getConfig().setShards(agentList);
            }
        });
        verticalLayoutForm.addComponent(shardsColSel);

        grid.addComponent(verticalLayoutForm, 3, 0, 9, 9);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.TOP_CENTER);

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                mongoWizard.back();
            }
        });
        Button next = new Button("Finish");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (Util.isStringEmpty(mongoWizard.getConfig().getReplicaSetName())) {
                    show("Please provide replica set name");
                } else if (Util.isCollectionEmpty(mongoWizard.getConfig().getShards())) {
                    show("Please add shards");
                } else {
                    show("INSTALLATION STARTED!!!");
                }
            }
        });

        verticalLayout.addComponent(grid);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);

        //add sample data=======================================================
        Agent agent1 = new Agent();
        agent1.setHostname("AGENT-1");
        agent1.setUuid(java.util.UUID.fromString("2ea0b741-73e4-44fc-9663-5a49dfd69ac8"));
        Agent agent2 = new Agent();
        agent2.setUuid(java.util.UUID.fromString("26753a44-e51c-4b93-b303-4fbedaef8e22"));
        agent2.setHostname("AGENT-2");
        List<Agent> sampleAgents = new ArrayList<Agent>();
        sampleAgents.add(agent1);
        sampleAgents.add(agent2);
        shardsColSel.setContainerDataSource(
                new BeanItemContainer<Agent>(
                        Agent.class, sampleAgents));
        //add sample data=======================================================

        //set values if this is back button
        replicaNameTxtFld.setValue(mongoWizard.getConfig().getReplicaSetName());
        shardsColSel.setValue(mongoWizard.getConfig().getShards());
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
