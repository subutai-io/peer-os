/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

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
public class Step2 extends Panel {

    public Step2(final MongoWizard mongoWizard) {

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.setMargin(true);

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

        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setSpacing(true);

        final TextField clusterNameTxtFld = new TextField("Enter cluster name");
        clusterNameTxtFld.setInputPrompt("Cluster name");
        clusterNameTxtFld.setRequired(true);
        clusterNameTxtFld.setMaxLength(20);

        mainContent.addComponent(clusterNameTxtFld);

        Label configServersLabel = new Label("<strong>Choose hosts that will act as config servers<br>"
                + "(Recommended 3 servers)</strong>");
        configServersLabel.setContentMode(Label.CONTENT_XHTML);
        mainContent.addComponent(configServersLabel);

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

        mainContent.addComponent(configServersColSel);

        Label routersLabel = new Label("<strong>Choose hosts that will act as routers<br>"
                + "(Provide at least 2 servers)</strong>");
        routersLabel.setContentMode(Label.CONTENT_XHTML);
        mainContent.addComponent(routersLabel);

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

        mainContent.addComponent(routersColSel);

        grid.addComponent(mainContent, 3, 0, 9, 9);
        grid.setComponentAlignment(mainContent, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                mongoWizard.getConfig().setClusterName(clusterNameTxtFld.getValue().toString().trim());
                mongoWizard.getConfig().setConfigServers((Set<Agent>) configServersColSel.getValue());
                mongoWizard.getConfig().setRouterServers((Set<Agent>) routersColSel.getValue());

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

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                mongoWizard.back();
            }
        });

        content.addComponent(grid);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(next);
        content.addComponent(buttons);

        addComponent(content);

        routersColSel.setContainerDataSource(
                new BeanItemContainer<Agent>(
                        Agent.class, mongoWizard.getConfig().getSelectedAgents()));
        configServersColSel.setContainerDataSource(
                new BeanItemContainer<Agent>(
                        Agent.class, mongoWizard.getConfig().getSelectedAgents()));

        //set values if this is a second visit
        clusterNameTxtFld.setValue(mongoWizard.getConfig().getClusterName());
        configServersColSel.setValue(Util.retainValues(mongoWizard.getConfig().getConfigServers(), mongoWizard.getConfig().getSelectedAgents()));
        routersColSel.setValue(Util.retainValues(mongoWizard.getConfig().getRouterServers(), mongoWizard.getConfig().getSelectedAgents()));
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
