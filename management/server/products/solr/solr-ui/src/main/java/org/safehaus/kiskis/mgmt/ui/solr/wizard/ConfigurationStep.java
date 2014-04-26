/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.solr.wizard;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.ui.*;

import java.util.Arrays;

/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel {

    public ConfigurationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(1, 3);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

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

        //configuration servers number
        ComboBox nodesCountCombo = new ComboBox("Choose number of nodes", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        nodesCountCombo.setMultiSelect(false);
        nodesCountCombo.setImmediate(true);
        nodesCountCombo.setTextInputAllowed(false);
        nodesCountCombo.setNullSelectionAllowed(false);
        nodesCountCombo.setValue(wizard.getConfig().getNumberOfNodes());

        nodesCountCombo.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setNumberOfNodes((Integer) event.getProperty().getValue());
            }
        });

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (Strings.isNullOrEmpty(wizard.getConfig().getClusterName())) {
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
        content.addComponent(nodesCountCombo);
        content.addComponent(buttons);

        addComponent(layout);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
