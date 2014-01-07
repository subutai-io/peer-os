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
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class Step3 extends Panel {

    public Step3(final Wizard wizard) {

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.setMargin(true);

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

        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setSpacing(true);

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

        mainContent.addComponent(replicaNameTxtFld);

        Label configServersLabel = new Label("<strong>Choose hosts that will act as data nodes<br>"
                + "(Choose odd number of servers, provide at least 1)</strong>");
        configServersLabel.setContentMode(Label.CONTENT_XHTML);
        mainContent.addComponent(configServersLabel);

        final TwinColSelect shardsColSel = new TwinColSelect("", new ArrayList<Agent>());
        shardsColSel.setItemCaptionPropertyId("hostname");
        shardsColSel.setRows(7);
        shardsColSel.setNullSelectionAllowed(true);
        shardsColSel.setMultiSelect(true);
        shardsColSel.setImmediate(true);
        shardsColSel.setRequired(true);
        shardsColSel.setLeftColumnCaption("Available Nodes");
        shardsColSel.setRightColumnCaption("Data Nodes");
        shardsColSel.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        shardsColSel.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Set<Agent> agentList = (Set<Agent>) event.getProperty().getValue();
                wizard.getConfig().setDataNodes(agentList);
            }
        });

        mainContent.addComponent(shardsColSel);

        grid.addComponent(mainContent, 3, 0, 9, 9);
        grid.setComponentAlignment(mainContent, Alignment.TOP_CENTER);

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
                wizard.getConfig().setReplicaSetName(replicaNameTxtFld.getValue().toString().trim());
                wizard.getConfig().setDataNodes((Set<Agent>) shardsColSel.getValue());

                if (Util.isStringEmpty(wizard.getConfig().getReplicaSetName())) {
                    show("Please provide replica set name");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getDataNodes())) {
                    show("Please add data nodes");
                } else if (wizard.getConfig().getDataNodes().size() % 2 == 0) {
                    show("Please add odd number of data nodes");
                } else {
                    wizard.next();
                }
            }
        });

        content.addComponent(grid);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(next);
        content.addComponent(buttons);

        addComponent(content);

        shardsColSel.setContainerDataSource(
                new BeanItemContainer<Agent>(
                        Agent.class, wizard.getConfig().getSelectedAgents()));

        //set values if this is a second visit
        replicaNameTxtFld.setValue(wizard.getConfig().getReplicaSetName());
        shardsColSel.setValue(Util.retainValues(wizard.getConfig().getDataNodes(), wizard.getConfig().getSelectedAgents()));
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
