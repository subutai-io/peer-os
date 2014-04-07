/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.shark.wizard;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.safehaus.kiskis.mgmt.api.spark.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.shark.SharkUI;

/**
 *
 * @author dilshat
 */
public class ConfigurationStep extends Panel {

    private final ComboBox sparkClusters;
    private final TwinColSelect select;

    public ConfigurationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(1, 2);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

        sparkClusters = new ComboBox("Spark cluster");
        select = new TwinColSelect("Nodes", new ArrayList<Agent>());

        sparkClusters.setMultiSelect(false);
        sparkClusters.setImmediate(true);
        sparkClusters.setTextInputAllowed(false);
        sparkClusters.setRequired(true);
        sparkClusters.setNullSelectionAllowed(false);

        List<Config> clusters = SharkUI.getDbManager().
                getInfo(Config.PRODUCT_KEY, Config.class);
        if (clusters.size() > 0) {
            for (Config hadoopClusterInfo : clusters) {
                sparkClusters.addItem(hadoopClusterInfo);
                sparkClusters.setItemCaption(hadoopClusterInfo,
                        hadoopClusterInfo.getClusterName());
            }
        }

        Config info = SharkUI.getDbManager().
                getInfo(Config.PRODUCT_KEY,
                        wizard.getConfig().getClusterName(),
                        Config.class);

        if (info != null) {
            sparkClusters.setValue(info);
        } else if (clusters.size() > 0) {
            sparkClusters.setValue(clusters.iterator().next());
        }

        if (sparkClusters.getValue() != null) {
            Config hadoopInfo = (Config) sparkClusters.getValue();
            wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
            select.setContainerDataSource(
                    new BeanItemContainer<Agent>(
                            Agent.class, hadoopInfo.getAllNodes()));
        }

        sparkClusters.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Config hadoopInfo = (Config) event.getProperty().getValue();
                    select.setValue(null);
                    select.setContainerDataSource(
                            new BeanItemContainer<Agent>(
                                    Agent.class, hadoopInfo.getAllNodes()));
                    wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
                    wizard.getConfig().setNodes(new HashSet<Agent>());
                }
            }
        });

        select.setItemCaptionPropertyId("hostname");
        select.setRows(7);
        select.setNullSelectionAllowed(false);
        select.setMultiSelect(true);
        select.setImmediate(true);
        select.setLeftColumnCaption("Available Nodes");
        select.setRightColumnCaption("Selected Nodes");
        select.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        select.setRequired(true);
        if (!Util.isCollectionEmpty(wizard.getConfig().getNodes())) {
            select.setValue(wizard.getConfig().getNodes());
        }
        select.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Collection) event.getProperty().getValue());
                    wizard.getConfig().setNodes(agentList);
                }
            }
        });

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (Util.isStringEmpty(wizard.getConfig().getClusterName())) {
                    show("Please, select Spark cluster");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getNodes())) {
                    show("Please, select target nodes");
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

        content.addComponent(sparkClusters);
        content.addComponent(select);
        content.addComponent(buttons);

        addComponent(layout);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
