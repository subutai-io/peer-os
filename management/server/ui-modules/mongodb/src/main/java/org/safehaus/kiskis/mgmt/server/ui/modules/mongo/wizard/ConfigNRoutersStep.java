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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.MongoDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class ConfigNRoutersStep extends Panel {

    Property.ValueChangeListener configChangeListener = null;
    Property.ValueChangeListener routersChangeListener = null;

    public ConfigNRoutersStep(final Wizard wizard) {

        setSizeFull();

        GridLayout grid = new GridLayout(10, 10);
        grid.setSpacing(true);
        grid.setMargin(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Installation Wizard<br>"
                + " 1) <font color=\"#f14c1a\"><strong>Config Servers and Routers</strong></font><br>"
                + " 2) Replica Set Configurations");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 1, 8);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        final TextField clusterNameTxtFld = new TextField("Enter cluster name");
        clusterNameTxtFld.setInputPrompt("Cluster name");
        clusterNameTxtFld.setRequired(true);
        clusterNameTxtFld.setMaxLength(20);
        clusterNameTxtFld.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setClusterName(event.getProperty().getValue().toString().trim());
            }
        });

        grid.addComponent(clusterNameTxtFld, 2, 0, 3, 0);

        Label configServersLabel = new Label("<strong>Choose hosts that will act as config servers "
                + "(Recommended 3 nodes, choose 1 or 3 nodes)</strong>");
        configServersLabel.setContentMode(Label.CONTENT_XHTML);
        grid.addComponent(configServersLabel, 2, 1, 9, 1);
        final Container routersSource = new BeanItemContainer<Agent>(
                Agent.class, wizard.getConfig().getSelectedAgents());
        final Container configSrvSource = new BeanItemContainer<Agent>(
                Agent.class, wizard.getConfig().getSelectedAgents());
        final TwinColSelect routersColSel = new TwinColSelect("", new ArrayList<Agent>());
        final TwinColSelect configServersColSel = new TwinColSelect("", new ArrayList<Agent>());

        configChangeListener = new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Set<Agent>) event.getProperty().getValue());
                    wizard.getConfig().setConfigServers(agentList);
                    //clean 
                    Util.removeValues(wizard.getConfig().getRouterServers(), wizard.getConfig().getConfigServers());
                    Util.removeValues(wizard.getConfig().getDataNodes(), wizard.getConfig().getConfigServers());
                    routersColSel.removeListener(routersChangeListener);
                    routersColSel.setValue(wizard.getConfig().getRouterServers());
                    routersColSel.addListener(routersChangeListener);
                    //update source
                    agentList = new HashSet(wizard.getConfig().getSelectedAgents());
                    Util.removeValues(agentList, wizard.getConfig().getConfigServers());
                    Util.removeValues(agentList, wizard.getConfig().getDataNodes());
                    routersSource.removeAllItems();
                    for (Agent agent : agentList) {
                        routersSource.addItem(agent);
                    }
                }
            }
        };
        routersChangeListener = new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Set<Agent>) event.getProperty().getValue());
                    wizard.getConfig().setRouterServers(agentList);
                    //clean 
                    Util.removeValues(wizard.getConfig().getConfigServers(), wizard.getConfig().getRouterServers());
                    Util.removeValues(wizard.getConfig().getDataNodes(), wizard.getConfig().getRouterServers());
                    configServersColSel.removeListener(configChangeListener);
                    configServersColSel.setValue(wizard.getConfig().getConfigServers());
                    configServersColSel.addListener(configChangeListener);
                    //update source
                    agentList = new HashSet(wizard.getConfig().getSelectedAgents());
                    Util.removeValues(agentList, wizard.getConfig().getRouterServers());
                    Util.removeValues(agentList, wizard.getConfig().getDataNodes());
                    configSrvSource.removeAllItems();
                    for (Agent agent : agentList) {
                        configSrvSource.addItem(agent);
                    }
                }
            }
        };

        configServersColSel.setItemCaptionPropertyId("hostname");
        configServersColSel.setRows(5);
        configServersColSel.setNullSelectionAllowed(true);
        configServersColSel.setMultiSelect(true);
        configServersColSel.setImmediate(true);
        configServersColSel.setLeftColumnCaption("Available Nodes");
        configServersColSel.setRightColumnCaption("Config Servers");
        configServersColSel.setSizeFull();
        configServersColSel.setRequired(true);
        configServersColSel.addListener(configChangeListener);

        grid.addComponent(configServersColSel, 2, 2, 9, 4);

        Label routersLabel = new Label("<strong>Choose hosts that will act as routers "
                + "(Provide at least 2 servers)</strong>");
        routersLabel.setContentMode(Label.CONTENT_XHTML);
        grid.addComponent(routersLabel, 2, 5, 9, 5);

        routersColSel.setItemCaptionPropertyId("hostname");
        routersColSel.setRows(5);
        routersColSel.setNullSelectionAllowed(true);
        routersColSel.setMultiSelect(true);
        routersColSel.setImmediate(true);
        routersColSel.setLeftColumnCaption("Available Nodes");
        routersColSel.setRightColumnCaption("Routers");
        routersColSel.setSizeFull();
        routersColSel.setRequired(true);
        routersColSel.addListener(routersChangeListener);

        grid.addComponent(routersColSel, 2, 6, 9, 8);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (Util.isStringEmpty(wizard.getConfig().getClusterName())) {
                    show("Please provide cluster name");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getConfigServers())) {
                    show("Please add config servers");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getRouterServers())) {
                    show("Please add routers");
                } else if (MongoDAO.getMongoClusterInfo(wizard.getConfig().getClusterName()) != null) {
                    show(MessageFormat.format("Cluster with name {0} already exists", wizard.getConfig().getClusterName()));
                } else if (wizard.getConfig().getConfigServers().size() != 1
                        && wizard.getConfig().getConfigServers().size() != 3) {
                    show("Please, select 1 or 3 nodes as config servers");
                } else {
                    wizard.next();
                }
            }
        });

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(next);
        grid.addComponent(buttons, 0, 9, 2, 9);

        addComponent(grid);

        routersColSel.setContainerDataSource(routersSource);

        configServersColSel.setContainerDataSource(configSrvSource);

        //set values if this is a second visit
        clusterNameTxtFld.setValue(wizard.getConfig().getClusterName());

        configServersColSel.setValue(wizard.getConfig().getConfigServers());
        routersColSel.setValue(wizard.getConfig().getRouterServers());

        //update sources
        Set<Agent> agentList = new HashSet(wizard.getConfig().getSelectedAgents());
        Util.removeValues(agentList, wizard.getConfig().getConfigServers());
        Util.removeValues(agentList, wizard.getConfig().getDataNodes());
        routersSource.removeAllItems();
        for (Agent agent : agentList) {
            routersSource.addItem(agent);
        }
        agentList = new HashSet(wizard.getConfig().getSelectedAgents());
        Util.removeValues(agentList, wizard.getConfig().getRouterServers());
        Util.removeValues(agentList, wizard.getConfig().getDataNodes());
        configSrvSource.removeAllItems();
        for (Agent agent : agentList) {
            configSrvSource.addItem(agent);
        }
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
