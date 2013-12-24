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
public class Step2 extends Panel {

    public Step2(final CassandraWizard mongoWizard) {

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(10, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Installation Wizard<br>"
                + " 1) <font color=\"#f14c1a\"><strong>Config Servers and Routers</strong></font><br>"
                + " 2) Replica Set Configurations");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 2, 1);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        VerticalLayout verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayoutForm.setSpacing(true);

        final TextField clusterNameTxtFld = new TextField("Enter cluster name");
        clusterNameTxtFld.setInputPrompt("Cluster name");
        clusterNameTxtFld.setRequired(true);
        clusterNameTxtFld.setMaxLength(30);
        clusterNameTxtFld.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                mongoWizard.getConfig().setClusterName(event.getProperty().getValue().toString().trim());
            }
        });
        verticalLayoutForm.addComponent(clusterNameTxtFld);

        Label configServersLabel = new Label("<strong>Choose hosts that will act as config servers<br>"
                + "(Recommended 3 servers)</strong>");
        configServersLabel.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(configServersLabel);

        final TwinColSelect configServersColSel = new TwinColSelect("", new ArrayList<Agent>());
        configServersColSel.setItemCaptionPropertyId("hostname");
        configServersColSel.setRows(7);
        configServersColSel.setNullSelectionAllowed(true);
        configServersColSel.setMultiSelect(true);
        configServersColSel.setImmediate(true);
        configServersColSel.setLeftColumnCaption("Available Nodes");
        configServersColSel.setRightColumnCaption("Config Servers");
        configServersColSel.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        configServersColSel.setRequired(true);
        configServersColSel.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Set<Agent> agentList = (Set<Agent>) event.getProperty().getValue();
                mongoWizard.getConfig().setConfigServers(agentList);
            }
        });
        verticalLayoutForm.addComponent(configServersColSel);

        Label routersLabel = new Label("<strong>Choose hosts that will act as routers<br>"
                + "(Provide at least 2 servers)</strong>");
        routersLabel.setContentMode(Label.CONTENT_XHTML);
        verticalLayoutForm.addComponent(routersLabel);

        final TwinColSelect routersColSel = new TwinColSelect("", new ArrayList<Agent>());
        routersColSel.setItemCaptionPropertyId("hostname");
        routersColSel.setRows(7);
        routersColSel.setNullSelectionAllowed(true);
        routersColSel.setMultiSelect(true);
        routersColSel.setImmediate(true);
        routersColSel.setLeftColumnCaption("Available Nodes");
        routersColSel.setRightColumnCaption("Routers");
        routersColSel.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        routersColSel.setRequired(true);
        routersColSel.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Set<Agent> agentList = (Set<Agent>) event.getProperty().getValue();
                mongoWizard.getConfig().setRouterServers(agentList);
            }
        });
        verticalLayoutForm.addComponent(routersColSel);

        grid.addComponent(verticalLayoutForm, 3, 0, 9, 9);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (Util.isStringEmpty(mongoWizard.getConfig().getClusterName())) {
                    show("Please provide cluster name");
                } else if (Util.isCollectionEmpty(mongoWizard.getConfig().getConfigServers())) {
                    show("Please add config servers");
                } else if (Util.isCollectionEmpty(mongoWizard.getConfig().getRouterServers())) {
                    show("Please add routers");
                } else {
                    mongoWizard.next();
                }
            }
        });

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(next);

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
        routersColSel.setContainerDataSource(
                new BeanItemContainer<Agent>(
                        Agent.class, sampleAgents));
        configServersColSel.setContainerDataSource(
                new BeanItemContainer<Agent>(
                        Agent.class, sampleAgents));
        //add sample data=======================================================

        //set values if this is back button
        clusterNameTxtFld.setValue(mongoWizard.getConfig().getClusterName());
        configServersColSel.setValue(mongoWizard.getConfig().getConfigServers());
        routersColSel.setValue(mongoWizard.getConfig().getRouterServers());
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
