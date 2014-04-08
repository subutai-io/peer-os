/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.presto.wizard;

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
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.presto.PrestoUI;

/**
 *
 * @author dilshat
 */
public class ConfigurationStep extends Panel {

    private final ComboBox hadoopClustersCombo;
    private final TwinColSelect workersSelect;
    private final ComboBox coordinatorNodeCombo;

    public ConfigurationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(1, 4);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

        hadoopClustersCombo = new ComboBox("Hadoop cluster");
        coordinatorNodeCombo = new ComboBox("Coordinator");
        workersSelect = new TwinColSelect("Workers", new ArrayList<Agent>());

        coordinatorNodeCombo.setMultiSelect(false);
        coordinatorNodeCombo.setImmediate(true);
        coordinatorNodeCombo.setTextInputAllowed(false);
        coordinatorNodeCombo.setRequired(true);
        coordinatorNodeCombo.setNullSelectionAllowed(false);

        hadoopClustersCombo.setMultiSelect(false);
        hadoopClustersCombo.setImmediate(true);
        hadoopClustersCombo.setTextInputAllowed(false);
        hadoopClustersCombo.setRequired(true);
        hadoopClustersCombo.setNullSelectionAllowed(false);

        workersSelect.setItemCaptionPropertyId("hostname");
        workersSelect.setRows(7);
        workersSelect.setNullSelectionAllowed(false);
        workersSelect.setMultiSelect(true);
        workersSelect.setImmediate(true);
        workersSelect.setLeftColumnCaption("Available Nodes");
        workersSelect.setRightColumnCaption("Selected Nodes");
        workersSelect.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        workersSelect.setRequired(true);

        List<HadoopClusterInfo> clusters = PrestoUI.getDbManager().
                getInfo(HadoopClusterInfo.SOURCE, HadoopClusterInfo.class);
        if (clusters.size() > 0) {
            for (HadoopClusterInfo hadoopClusterInfo : clusters) {
                hadoopClustersCombo.addItem(hadoopClusterInfo);
                hadoopClustersCombo.setItemCaption(hadoopClusterInfo,
                        hadoopClusterInfo.getClusterName());
            }
        }

        HadoopClusterInfo info = PrestoUI.getDbManager().
                getInfo(HadoopClusterInfo.SOURCE,
                        wizard.getConfig().getClusterName(),
                        HadoopClusterInfo.class);

        if (info != null) {
            hadoopClustersCombo.setValue(info);
        } else if (clusters.size() > 0) {
            hadoopClustersCombo.setValue(clusters.iterator().next());
        }

        if (hadoopClustersCombo.getValue() != null) {
            HadoopClusterInfo hadoopInfo = (HadoopClusterInfo) hadoopClustersCombo.getValue();
            wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
            workersSelect.setContainerDataSource(
                    new BeanItemContainer<Agent>(
                            Agent.class, hadoopInfo.getAllAgents()));
            for (Agent agent : hadoopInfo.getAllAgents()) {
                coordinatorNodeCombo.addItem(agent);
                coordinatorNodeCombo.setItemCaption(agent, agent.getHostname());
            }
        }

        hadoopClustersCombo.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    HadoopClusterInfo hadoopInfo = (HadoopClusterInfo) event.getProperty().getValue();
                    workersSelect.setValue(null);
                    workersSelect.setContainerDataSource(
                            new BeanItemContainer<Agent>(
                                    Agent.class, hadoopInfo.getAllAgents()));
                    coordinatorNodeCombo.setValue(null);
                    for (Agent agent : hadoopInfo.getAllAgents()) {
                        coordinatorNodeCombo.addItem(agent);
                        coordinatorNodeCombo.setItemCaption(agent, agent.getHostname());
                    }
                    wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
                    wizard.getConfig().setWorkers(new HashSet<Agent>());
                    wizard.getConfig().setCoordinatorNode(null);
                }
            }
        });

        if (wizard.getConfig().getCoordinatorNode() != null) {
            coordinatorNodeCombo.setValue(wizard.getConfig().getCoordinatorNode());
        }

        coordinatorNodeCombo.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Agent coordinator = (Agent) event.getProperty().getValue();
                    wizard.getConfig().setCoordinatorNode(coordinator);
                }
            }
        });

        if (!Util.isCollectionEmpty(wizard.getConfig().getWorkers())) {
            workersSelect.setValue(wizard.getConfig().getWorkers());
        }
        workersSelect.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Collection) event.getProperty().getValue());
                    wizard.getConfig().setWorkers(agentList);
                }
            }
        });

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (Util.isStringEmpty(wizard.getConfig().getClusterName())) {
                    show("Please, select Hadoop cluster");
                } else if (wizard.getConfig().getCoordinatorNode() == null) {
                    show("Please, select coordinator node");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getWorkers())) {
                    show("Please, select worker nodes");
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

        content.addComponent(hadoopClustersCombo);
        content.addComponent(coordinatorNodeCombo);
        content.addComponent(workersSelect);
        content.addComponent(buttons);

        addComponent(layout);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
