/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.hadoop.wizard;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.ui.*;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.Arrays;

/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel {

    public ConfigurationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(2, 7);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

        final TextField clusterNameTxtFld = new TextField("Enter cluster name");
        clusterNameTxtFld.setInputPrompt("Cluster name");
        clusterNameTxtFld.setRequired(true);
        clusterNameTxtFld.setMaxLength(20);
        if (!Strings.isNullOrEmpty(wizard.getConfig().getClusterName())) {
            clusterNameTxtFld.setValue(wizard.getConfig().getClusterName());
        }
        clusterNameTxtFld.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setClusterName(event.getProperty().getValue().toString().trim());
            }
        });

        //configuration servers number
        ComboBox slaveNodesComboBox = new ComboBox("Choose number of slave nodes", Arrays.asList(1, 2, 3, 4, 5));
        slaveNodesComboBox.setMultiSelect(false);
        slaveNodesComboBox.setImmediate(true);
        slaveNodesComboBox.setTextInputAllowed(false);
        slaveNodesComboBox.setNullSelectionAllowed(false);
        slaveNodesComboBox.setValue(wizard.getConfig().getCountOfSlaveNodes());

        slaveNodesComboBox.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setCountOfSlaveNodes((Integer) event.getProperty().getValue());
            }
        });

        //configuration replication factor
        ComboBox replicationFactorComboBox = new ComboBox("Choose replication factor for slave nodes", Arrays.asList(1, 2, 3, 4, 5));
        replicationFactorComboBox.setMultiSelect(false);
        replicationFactorComboBox.setImmediate(true);
        replicationFactorComboBox.setTextInputAllowed(false);
        replicationFactorComboBox.setNullSelectionAllowed(false);
        replicationFactorComboBox.setValue(wizard.getConfig().getReplicationFactor());

        replicationFactorComboBox.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setReplicationFactor((Integer) event.getProperty().getValue());
            }
        });

        TextField domain = new TextField("Enter domain name");
        domain.setInputPrompt(wizard.getConfig().getDomainName());
        domain.setValue(wizard.getConfig().getDomainName());
        domain.setMaxLength(20);
        domain.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String value = event.getProperty().getValue().toString().trim();
                if (!Util.isStringEmpty(value)) {
                    wizard.getConfig().setDomainName(value);
                }
            }
        });

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (Util.isStringEmpty(wizard.getConfig().getClusterName())) {
                    show("Please provide cluster name");
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

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.addComponent(new Label("Please, specify installation settings"));
        layout.addComponent(content);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(next);

        content.addComponent(clusterNameTxtFld);
        content.addComponent(domain);
        content.addComponent(slaveNodesComboBox);
        content.addComponent(replicationFactorComboBox);
        content.addComponent(buttons);

        addComponent(layout);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
