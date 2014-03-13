/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class ReplicaSetStep extends Panel {

    public ReplicaSetStep(final Wizard wizard) {

        setSizeFull();

        GridLayout grid = new GridLayout(10, 10);
        grid.setSpacing(true);
        grid.setMargin(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Installation Wizard<br>"
                + " 1) <strong>Config Servers and Routers<br>"
                + " 2) <font color=\"#f14c1a\">Replica Set Configurations</strong></font>");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 1, 8);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        final TextField replicaNameTxtFld = new TextField("Enter Replica Set name");
        replicaNameTxtFld.setInputPrompt("Replica Set name");
        replicaNameTxtFld.setRequired(true);
        replicaNameTxtFld.setMaxLength(20);
        replicaNameTxtFld.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setReplicaSetName(event.getProperty().getValue().toString().trim());
            }
        });

        grid.addComponent(replicaNameTxtFld, 2, 0, 3, 0);

        Label configServersLabel = new Label("<strong>Choose hosts that will act as data nodes "
                + "(Choose odd number of servers: 3 to 7)</strong>");
        configServersLabel.setContentMode(Label.CONTENT_XHTML);
        grid.addComponent(configServersLabel, 2, 1, 9, 1);

        final TwinColSelect shardsColSel = new TwinColSelect("", new ArrayList<Agent>());
        shardsColSel.setItemCaptionPropertyId("hostname");
        shardsColSel.setRows(10);
        shardsColSel.setNullSelectionAllowed(true);
        shardsColSel.setMultiSelect(true);
        shardsColSel.setImmediate(true);
        shardsColSel.setRequired(true);
        shardsColSel.setLeftColumnCaption("Available Nodes");
        shardsColSel.setRightColumnCaption("Data Nodes");
        shardsColSel.setSizeFull();
        shardsColSel.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Set<Agent>) event.getProperty().getValue());
                    wizard.getConfig().setDataNodes(agentList);
                    //clean 
                    Util.removeValues(wizard.getConfig().getConfigServers(), wizard.getConfig().getDataNodes());
                    Util.removeValues(wizard.getConfig().getRouterServers(), wizard.getConfig().getDataNodes());
                }
            }
        });

        grid.addComponent(shardsColSel, 2, 2, 9, 8);

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });
        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (Util.isStringEmpty(wizard.getConfig().getReplicaSetName())) {
                    show("Please provide replica set name");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getDataNodes())) {
                    show("Please add data nodes");
                } else if (wizard.getConfig().getDataNodes().size() < 3) {
                    show("Please add at least 3 data nodes");
                } else if (wizard.getConfig().getDataNodes().size() % 2 == 0) {
                    show("Please add odd number of data nodes");
                } else if (wizard.getConfig().getDataNodes().size() > 7) {
                    show("Please add no more than 7 data nodes");
                } else {
                    wizard.next();
                }
            }
        });

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(next);
        grid.addComponent(buttons, 0, 9, 2, 9);

        addComponent(grid);
        Container shardsSource = new BeanItemContainer<Agent>(
                Agent.class, wizard.getConfig().getSelectedAgents());
        shardsColSel.setContainerDataSource(shardsSource
        );

        //set values if this is a second visit
        replicaNameTxtFld.setValue(wizard.getConfig().getReplicaSetName());
        shardsColSel.setValue(wizard.getConfig().getDataNodes());
        //update sources
        Set<Agent> agentList = new HashSet(wizard.getConfig().getSelectedAgents());
        Util.removeValues(agentList, wizard.getConfig().getConfigServers());
        Util.removeValues(agentList, wizard.getConfig().getRouterServers());
        shardsSource.removeAllItems();
        for (Agent agent : agentList) {
            shardsSource.addItem(agent);
        }
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
