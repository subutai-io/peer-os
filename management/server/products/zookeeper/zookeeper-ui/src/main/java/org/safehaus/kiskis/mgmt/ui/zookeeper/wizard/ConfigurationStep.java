/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.zookeeper.wizard;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.zookeeper.ZookeeperUI;

import java.util.*;

/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel {

    public ConfigurationStep(final Wizard wizard) {

        setSizeFull();

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName(Runo.SPLITPANEL_SMALL);
//        horizontalSplit.setSplitPosition(200, UNITS_PIXELS);

        //Standalone cluster installation controls

        GridLayout standaloneInstallationControls = new GridLayout(1, 4);
        standaloneInstallationControls.setSizeFull();
        standaloneInstallationControls.setSpacing(true);
        standaloneInstallationControls.setMargin(true);

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
        final TextField zkNameTxtFld = new TextField("Enter zk name");
        zkNameTxtFld.setInputPrompt("ZK name");
        zkNameTxtFld.setRequired(true);
        zkNameTxtFld.setMaxLength(20);
        zkNameTxtFld.setValue(wizard.getConfig().getZkName());
        zkNameTxtFld.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setZkName(event.getProperty().getValue().toString().trim());
            }
        });

        //number of nodes
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

                } else if (Strings.isNullOrEmpty(wizard.getConfig().getZkName())) {
                    show("Please provide zk name");

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

        standaloneInstallationControls.addComponent(clusterNameTxtFld);
        standaloneInstallationControls.addComponent(zkNameTxtFld);
        standaloneInstallationControls.addComponent(nodesCountCombo);
        standaloneInstallationControls.addComponent(buttons);


        //Over Hadoop cluster installation controls

        GridLayout overHadoopInstallationControls = new GridLayout(1, 2);
        overHadoopInstallationControls.setSizeFull();
        overHadoopInstallationControls.setSpacing(true);
        overHadoopInstallationControls.setMargin(true);


        ComboBox hadoopClustersCombo = new ComboBox("Hadoop cluster");
        final TwinColSelect hadoopNodesSelect = new TwinColSelect("Nodes", new ArrayList<Agent>());

        hadoopClustersCombo.setMultiSelect(false);
        hadoopClustersCombo.setImmediate(true);
        hadoopClustersCombo.setTextInputAllowed(false);
        hadoopClustersCombo.setRequired(true);
        hadoopClustersCombo.setNullSelectionAllowed(false);

        List<Config> clusters = ZookeeperUI.getHadoopManager().getClusters();
        if (clusters.size() > 0) {
            for (Config hadoopClusterInfo : clusters) {
                hadoopClustersCombo.addItem(hadoopClusterInfo);
                hadoopClustersCombo.setItemCaption(hadoopClusterInfo,
                        hadoopClusterInfo.getClusterName());
            }
        }

        Config info = ZookeeperUI.getHadoopManager().getCluster(wizard.getConfig().getClusterName());

        if (info != null) {
            hadoopClustersCombo.setValue(info);
        } else if (clusters.size() > 0) {
            hadoopClustersCombo.setValue(clusters.iterator().next());
        }

        if (hadoopClustersCombo.getValue() != null) {
            Config hadoopInfo = (Config) hadoopClustersCombo.getValue();
            wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
            hadoopNodesSelect.setContainerDataSource(
                    new BeanItemContainer<Agent>(
                            Agent.class, hadoopInfo.getAllNodes())
            );
        }

        hadoopClustersCombo.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Config hadoopInfo = (Config) event.getProperty().getValue();
                    hadoopNodesSelect.setValue(null);
                    hadoopNodesSelect.setContainerDataSource(
                            new BeanItemContainer<Agent>(
                                    Agent.class, hadoopInfo.getAllNodes())
                    );
                    wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
                    wizard.getConfig().setNodes(new HashSet<Agent>());
                }
            }
        });

        hadoopNodesSelect.setItemCaptionPropertyId("hostname");
        hadoopNodesSelect.setRows(7);
        hadoopNodesSelect.setMultiSelect(true);
        hadoopNodesSelect.setImmediate(true);
        hadoopNodesSelect.setLeftColumnCaption("Available Nodes");
        hadoopNodesSelect.setRightColumnCaption("Selected Nodes");
        hadoopNodesSelect.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        hadoopNodesSelect.setRequired(true);
        if (!Util.isCollectionEmpty(wizard.getConfig().getNodes())) {
            hadoopNodesSelect.setValue(wizard.getConfig().getNodes());
        }
        hadoopNodesSelect.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Collection) event.getProperty().getValue());
                    wizard.getConfig().setNodes(agentList);
                }
            }
        });

        Button next2 = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (Strings.isNullOrEmpty(wizard.getConfig().getClusterName())) {
                    show("Please, select Hadoop cluster");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getNodes())) {
                    show("Please, select target nodes");
                } else {
                    wizard.next();
                }
            }
        });

        Button back2 = new Button("Back");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });

        HorizontalLayout buttons2 = new HorizontalLayout();
        buttons.addComponent(back2);
        buttons.addComponent(next2);

        overHadoopInstallationControls.addComponent(hadoopClustersCombo);
        overHadoopInstallationControls.addComponent(hadoopNodesSelect);
        overHadoopInstallationControls.addComponent(buttons2);


        horizontalSplit.setFirstComponent(standaloneInstallationControls);
        horizontalSplit.setSecondComponent(overHadoopInstallationControls);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.addComponent(new Label("Please, specify installation settings"));
        layout.addComponent(horizontalSplit);

        addComponent(layout);

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
