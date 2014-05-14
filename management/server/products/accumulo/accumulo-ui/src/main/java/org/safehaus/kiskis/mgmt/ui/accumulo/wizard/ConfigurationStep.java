/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.accumulo.wizard;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.accumulo.AccumuloUI;
import org.safehaus.kiskis.mgmt.ui.accumulo.common.UiUtil;

import java.util.*;

/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel {
    final Property.ValueChangeListener masterNodeComboChangeListener;
    final Property.ValueChangeListener gcNodeComboChangeListener;

    public ConfigurationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(4, 4);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

        //hadoop combo
        final ComboBox hadoopClustersCombo = UiUtil.getCombo("Hadoop cluster");
        //master nodes
        final ComboBox masterNodeCombo = UiUtil.getCombo("Master node");
        final ComboBox gcNodeCombo = UiUtil.getCombo("GC node");
        final ComboBox monitorNodeCombo = UiUtil.getCombo("Monitor node");
        final TwinColSelect tracersSelect = UiUtil.getTwinSelect("Tracers", "hostname", "Available Nodes", "Selected Nodes", 4);
        //slave nodes
        final TwinColSelect slavesSelect = UiUtil.getTwinSelect("Slaves", "hostname", "Available Nodes", "Selected Nodes", 4);

        //get hadoop clusters from db
        List<org.safehaus.kiskis.mgmt.api.hadoop.Config> hadoopClusters = AccumuloUI.getHadoopManager().getClusters();
        final List<org.safehaus.kiskis.mgmt.api.zookeeper.Config> zkClusters = AccumuloUI.getZookeeperManager().getClusters();
        Set<org.safehaus.kiskis.mgmt.api.hadoop.Config> filteredHadoopClusters = new HashSet<>();

        //filter out those hadoop clusters which have zk clusters installed on top
        for (org.safehaus.kiskis.mgmt.api.hadoop.Config hadoopClusterInfo : hadoopClusters) {
            for (org.safehaus.kiskis.mgmt.api.zookeeper.Config zkClusterInfo : zkClusters) {
                if (hadoopClusterInfo.getClusterName().equals(zkClusterInfo.getClusterName()) && !zkClusterInfo.isStandalone()) {
                    filteredHadoopClusters.add(hadoopClusterInfo);
                    break;
                }
            }
        }

        //fill hadoopClustersCombo with hadoop cluster infos
        if (filteredHadoopClusters.size() > 0) {
            for (org.safehaus.kiskis.mgmt.api.hadoop.Config hadoopClusterInfo : filteredHadoopClusters) {
                hadoopClustersCombo.addItem(hadoopClusterInfo);
                hadoopClustersCombo.setItemCaption(hadoopClusterInfo,
                        hadoopClusterInfo.getClusterName());
            }
        }

        //try to find hadoop cluster info based on one saved in the configuration
        org.safehaus.kiskis.mgmt.api.hadoop.Config info = AccumuloUI.getHadoopManager().getCluster(wizard.getConfig().getClusterName());

        //select if saved found
        if (info != null) {
            hadoopClustersCombo.setValue(info);
            hadoopClustersCombo.setItemCaption(info,
                    info.getClusterName());
        } else if (filteredHadoopClusters.size() > 0) {
            //select first one if saved not found
            hadoopClustersCombo.setValue(filteredHadoopClusters.iterator().next());
        }


        //fill selection controls with hadoop nodes
        if (hadoopClustersCombo.getValue() != null) {
            org.safehaus.kiskis.mgmt.api.hadoop.Config hadoopInfo = (org.safehaus.kiskis.mgmt.api.hadoop.Config) hadoopClustersCombo.getValue();

            wizard.getConfig().setClusterName(hadoopInfo.getClusterName());

            setComboDS(masterNodeCombo, filterAgents(hadoopInfo, zkClusters));
            setComboDS(gcNodeCombo, filterAgents(hadoopInfo, zkClusters));
            setComboDS(monitorNodeCombo, filterAgents(hadoopInfo, zkClusters));
            setTwinSelectDS(tracersSelect, filterAgents(hadoopInfo, zkClusters));
            setTwinSelectDS(slavesSelect, filterAgents(hadoopInfo, zkClusters));
        }

        //on hadoop cluster change reset all controls and config
        hadoopClustersCombo.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    org.safehaus.kiskis.mgmt.api.hadoop.Config hadoopInfo = (org.safehaus.kiskis.mgmt.api.hadoop.Config) event.getProperty().getValue();
                    setComboDS(masterNodeCombo, filterAgents(hadoopInfo, zkClusters));
                    setComboDS(gcNodeCombo, filterAgents(hadoopInfo, zkClusters));
                    setComboDS(monitorNodeCombo, filterAgents(hadoopInfo, zkClusters));
                    setTwinSelectDS(tracersSelect, filterAgents(hadoopInfo, zkClusters));
                    setTwinSelectDS(slavesSelect, filterAgents(hadoopInfo, zkClusters));
                    wizard.getConfig().reset();
                    wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
                }
            }
        });

        //restore master node if back button is pressed
        if (wizard.getConfig().getMasterNode() != null) {
            masterNodeCombo.setValue(wizard.getConfig().getMasterNode());
        }
        //restore gc node if back button is pressed
        if (wizard.getConfig().getGcNode() != null) {
            gcNodeCombo.setValue(wizard.getConfig().getGcNode());
        }
        //restore monitor node if back button is pressed
        if (wizard.getConfig().getMonitor() != null) {
            monitorNodeCombo.setValue(wizard.getConfig().getMonitor());
        }

        //add value change handler
        masterNodeComboChangeListener = new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Agent masterNode = (Agent) event.getProperty().getValue();
                    wizard.getConfig().setMasterNode(masterNode);
                    org.safehaus.kiskis.mgmt.api.hadoop.Config hadoopInfo = (org.safehaus.kiskis.mgmt.api.hadoop.Config) hadoopClustersCombo.getValue();
                    List<Agent> hadoopNodes = filterAgents(hadoopInfo, zkClusters);
                    hadoopNodes.remove(masterNode);
                    gcNodeCombo.removeListener(gcNodeComboChangeListener);
                    setComboDS(gcNodeCombo, hadoopNodes);
                    if (!masterNode.equals(wizard.getConfig().getGcNode())) {
                        gcNodeCombo.setValue(wizard.getConfig().getGcNode());
                    } else {
                        wizard.getConfig().setGcNode(null);
                    }
                    gcNodeCombo.addListener(gcNodeComboChangeListener);
                }
            }
        };
        masterNodeCombo.addListener(masterNodeComboChangeListener);
        //add value change handler
        gcNodeComboChangeListener = new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Agent gcNode = (Agent) event.getProperty().getValue();
                    wizard.getConfig().setGcNode(gcNode);
                    org.safehaus.kiskis.mgmt.api.hadoop.Config hadoopInfo = (org.safehaus.kiskis.mgmt.api.hadoop.Config) hadoopClustersCombo.getValue();
                    List<Agent> hadoopNodes = filterAgents(hadoopInfo, zkClusters);
                    hadoopNodes.remove(gcNode);
                    masterNodeCombo.removeListener(masterNodeComboChangeListener);
                    setComboDS(masterNodeCombo, hadoopNodes);
                    if (!gcNode.equals(wizard.getConfig().getMasterNode())) {
                        masterNodeCombo.setValue(wizard.getConfig().getMasterNode());
                    } else {
                        wizard.getConfig().setMasterNode(null);
                    }
                    masterNodeCombo.addListener(masterNodeComboChangeListener);
                }
            }
        };
        gcNodeCombo.addListener(gcNodeComboChangeListener);
        //add value change handler
        monitorNodeCombo.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Agent monitor = (Agent) event.getProperty().getValue();
                    wizard.getConfig().setMonitor(monitor);
                }
            }
        });

        //restore tracers if back button is pressed
        if (!Util.isCollectionEmpty(wizard.getConfig().getTracers())) {
            tracersSelect.setValue(wizard.getConfig().getTracers());
        }
        //restore slaves if back button is pressed
        if (!Util.isCollectionEmpty(wizard.getConfig().getSlaves())) {
            slavesSelect.setValue(wizard.getConfig().getSlaves());
        }

        //add value change handler
        tracersSelect.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Collection) event.getProperty().getValue());
                    wizard.getConfig().setTracers(agentList);
                }
            }
        });
        //add value change handler
        slavesSelect.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Collection) event.getProperty().getValue());
                    wizard.getConfig().setSlaves(agentList);
                }
            }
        });

        Button next = new Button("Next");
        //check valid configuration
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (Strings.isNullOrEmpty(wizard.getConfig().getClusterName())) {
                    show("Please, select Hadoop cluster");
                } else if (wizard.getConfig().getMasterNode() == null) {
                    show("Please, select master node");
                } else if (wizard.getConfig().getGcNode() == null) {
                    show("Please, select gc node");
                } else if (wizard.getConfig().getMonitor() == null) {
                    show("Please, select monitor");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getTracers())) {
                    show("Please, select tracer(s)");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getSlaves())) {
                    show("Please, select slave(s)");
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

        content.addComponent(hadoopClustersCombo, 0, 0);
        content.addComponent(masterNodeCombo, 1, 0);
        content.addComponent(gcNodeCombo, 2, 0);
        content.addComponent(monitorNodeCombo, 3, 0);
        content.addComponent(tracersSelect, 0, 1, 3, 1);
        content.addComponent(slavesSelect, 0, 2, 3, 2);
        content.addComponent(buttons, 0, 3, 3, 3);

        addComponent(layout);

    }

    private List<Agent> filterAgents(org.safehaus.kiskis.mgmt.api.hadoop.Config hadoopInfo, List<org.safehaus.kiskis.mgmt.api.zookeeper.Config> zkClusters) {

        List<Agent> filteredAgents = new ArrayList<>();
        org.safehaus.kiskis.mgmt.api.zookeeper.Config zkConfig = null;

        for (org.safehaus.kiskis.mgmt.api.zookeeper.Config zkInfo : zkClusters) {
            if (zkInfo.getClusterName().equals(hadoopInfo.getClusterName())) {
                zkConfig = zkInfo;
                break;
            }
        }

        if (zkConfig != null) {
            filteredAgents.addAll(hadoopInfo.getAllNodes());
            filteredAgents.retainAll(zkConfig.getNodes());
        }

        return filteredAgents;
    }

    private void setComboDS(ComboBox target, List<Agent> hadoopNodes) {
        target.removeAllItems();
        target.setValue(null);
        for (Agent agent : hadoopNodes) {
            target.addItem(agent);
            target.setItemCaption(agent, agent.getHostname());
        }
    }

    private void setTwinSelectDS(TwinColSelect target, List<Agent> hadoopNodes) {
        target.setValue(null);
        target.setContainerDataSource(
                new BeanItemContainer<>(
                        Agent.class, hadoopNodes)
        );

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
