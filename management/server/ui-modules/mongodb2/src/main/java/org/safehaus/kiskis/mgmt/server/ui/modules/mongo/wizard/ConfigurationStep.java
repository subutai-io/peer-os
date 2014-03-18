/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import java.util.Arrays;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.MongoDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class ConfigurationStep extends Panel {

    Property.ValueChangeListener configChangeListener = null;
    Property.ValueChangeListener routersChangeListener = null;

    public ConfigurationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout grid = new GridLayout(10, 10);
        grid.setSpacing(true);
        grid.setMargin(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Please, specify installation settings");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 1, 8);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        final TextField clusterNameTxtFld = new TextField("Enter cluster name");
        clusterNameTxtFld.setInputPrompt("Cluster name");
        clusterNameTxtFld.setRequired(true);
        clusterNameTxtFld.setMaxLength(20);
        clusterNameTxtFld.setValue(wizard.getConfig().getClusterName());
        clusterNameTxtFld.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setClusterName(event.getProperty().getValue().toString().trim());
            }
        });

        grid.addComponent(clusterNameTxtFld, 2, 0, 9, 0);

        //configuration servers number
        ComboBox cfgSrvsCombo = new ComboBox("Choose number of configuration servers (Recommended 3 nodes)", Arrays.asList(1, 3));
        cfgSrvsCombo.setMultiSelect(false);
        cfgSrvsCombo.setImmediate(true);
        cfgSrvsCombo.setTextInputAllowed(false);
        cfgSrvsCombo.setNullSelectionAllowed(false);
        cfgSrvsCombo.setValue(wizard.getConfig().getNumberOfConfigServers());

        cfgSrvsCombo.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setNumberOfConfigServers((Integer) event.getProperty().getValue());
            }
        });
        grid.addComponent(cfgSrvsCombo, 2, 1, 9, 1);

        //routers number
        ComboBox routersCombo = new ComboBox("Choose number of routers ( At least 2 recommended)", Arrays.asList(1, 2, 3));
        routersCombo.setMultiSelect(false);
        routersCombo.setImmediate(true);
        routersCombo.setTextInputAllowed(false);
        routersCombo.setNullSelectionAllowed(false);
        routersCombo.setValue(wizard.getConfig().getNumberOfRouters());

        routersCombo.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setNumberOfRouters((Integer) event.getProperty().getValue());
            }
        });
        grid.addComponent(routersCombo, 2, 2, 9, 2);

        //datanodes number
        ComboBox dataNodesCombo = new ComboBox("Choose number of datanodes ( At least 3 recommended)", Arrays.asList(1, 3, 5, 7));
        dataNodesCombo.setMultiSelect(false);
        dataNodesCombo.setImmediate(true);
        dataNodesCombo.setTextInputAllowed(false);
        dataNodesCombo.setNullSelectionAllowed(false);
        dataNodesCombo.setValue(wizard.getConfig().getNumberOfDataNodes());

        dataNodesCombo.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setNumberOfDataNodes((Integer) event.getProperty().getValue());
            }
        });
        grid.addComponent(dataNodesCombo, 2, 3, 9, 3);

        TextField replicaSetName = new TextField("Enter replica set name");
        replicaSetName.setInputPrompt("Replica set name");
        replicaSetName.setRequired(true);
        replicaSetName.setMaxLength(20);
        replicaSetName.setValue(wizard.getConfig().getReplicaSetName());
        replicaSetName.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setReplicaSetName(event.getProperty().getValue().toString().trim());
            }
        });

        grid.addComponent(replicaSetName, 2, 4, 9, 4);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (Util.isStringEmpty(wizard.getConfig().getClusterName())) {
                    show("Please provide cluster name");
                } else if (Util.isStringEmpty(wizard.getConfig().getReplicaSetName())) {
                    show("Please provide replica set name");
                } else if (MongoDAO.getMongoClusterInfo(wizard.getConfig().getClusterName()) != null) {
                    show(String.format("Cluster with name '%s' already exists", wizard.getConfig().getClusterName()));
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

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
