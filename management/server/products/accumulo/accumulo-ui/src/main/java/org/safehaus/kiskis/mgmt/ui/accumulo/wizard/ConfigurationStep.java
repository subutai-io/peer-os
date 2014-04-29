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
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.accumulo.AccumuloUI;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel {
    final Property.ValueChangeListener masterNodeComboChangeListener;
    final Property.ValueChangeListener gcNodeComboChangeListener;

    public ConfigurationStep(final Wizard wizard) {

        setSizeFull();

        GridLayout content = new GridLayout(3, 6);
        content.setSizeFull();
        content.setSpacing(true);
        content.setMargin(true);

        //hadoop combo
        final ComboBox hadoopClustersCombo = UiUtil.getCombo("Hadoop cluster");
        //master nodes
        final ComboBox masterNodeCombo = UiUtil.getCombo("Master node");
        final ComboBox gcNodeCombo = UiUtil.getCombo("GC node");
        final TwinColSelect tracersSelect = UiUtil.getTwinSelect("Tracers", "hostname", "Available Nodes", "Selected Nodes", 4);
        final TwinColSelect monitorsSelect = UiUtil.getTwinSelect("Monitors", "hostname", "Available Nodes", "Selected Nodes", 4);
        //slave nodes
        final TwinColSelect loggersSelect = UiUtil.getTwinSelect("Loggers", "hostname", "Available Nodes", "Selected Nodes", 4);
        final TwinColSelect tabletServers = UiUtil.getTwinSelect("Tablet servers", "hostname", "Available Nodes", "Selected Nodes", 4);

        //get hadoop clusters from db
        List<Config> clusters = AccumuloUI.getDbManager().
                getInfo(Config.PRODUCT_KEY, Config.class);

        //fill hadoopClustersCombo with hadoop cluster infos
        if (clusters.size() > 0) {
            for (Config hadoopClusterInfo : clusters) {
                hadoopClustersCombo.addItem(hadoopClusterInfo);
                hadoopClustersCombo.setItemCaption(hadoopClusterInfo,
                        hadoopClusterInfo.getClusterName());
            }
        }

        //try to find hadoop cluster info based on one saved in the configuration
        Config info = AccumuloUI.getDbManager().
                getInfo(Config.PRODUCT_KEY,
                        wizard.getConfig().getClusterName(),
                        Config.class);

        //select if saved found
        if (info != null) {
            hadoopClustersCombo.setValue(info);
            hadoopClustersCombo.setItemCaption(info,
                    info.getClusterName());
        } else if (clusters.size() > 0) {
            //select first one if saved not found
            hadoopClustersCombo.setValue(clusters.iterator().next());
        }

        //fill selection controls with hadoop nodes
        if (hadoopClustersCombo.getValue() != null) {
            Config hadoopInfo = (Config) hadoopClustersCombo.getValue();

            wizard.getConfig().setClusterName(hadoopInfo.getClusterName());

            setComboDS(masterNodeCombo, hadoopInfo.getAllNodes());
            setComboDS(gcNodeCombo, hadoopInfo.getAllNodes());
            setTwinSelectDS(tracersSelect, hadoopInfo.getAllNodes());
            setTwinSelectDS(monitorsSelect, hadoopInfo.getAllNodes());
            setTwinSelectDS(loggersSelect, hadoopInfo.getAllNodes());
            setTwinSelectDS(tabletServers, hadoopInfo.getAllNodes());
        }

        //on hadoop cluster change reset all controls and config
        hadoopClustersCombo.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Config hadoopInfo = (Config) event.getProperty().getValue();
                    setComboDS(masterNodeCombo, hadoopInfo.getAllNodes());
                    setComboDS(gcNodeCombo, hadoopInfo.getAllNodes());
                    setTwinSelectDS(tracersSelect, hadoopInfo.getAllNodes());
                    setTwinSelectDS(monitorsSelect, hadoopInfo.getAllNodes());
                    setTwinSelectDS(loggersSelect, hadoopInfo.getAllNodes());
                    setTwinSelectDS(tabletServers, hadoopInfo.getAllNodes());
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

        //add value change handler
        masterNodeComboChangeListener = new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Agent masterNode = (Agent) event.getProperty().getValue();
                    wizard.getConfig().setMasterNode(masterNode);
                    Config hadoopInfo = (Config) hadoopClustersCombo.getValue();
                    List<Agent> hadoopNodes = hadoopInfo.getAllNodes();
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
                    Config hadoopInfo = (Config) hadoopClustersCombo.getValue();
                    List<Agent> hadoopNodes = hadoopInfo.getAllNodes();
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

        //restore tracers if back button is pressed
        if (!Util.isCollectionEmpty(wizard.getConfig().getTracers())) {
            tracersSelect.setValue(wizard.getConfig().getTracers());
        }
        //restore monitors if back button is pressed
        if (!Util.isCollectionEmpty(wizard.getConfig().getLoggers())) {
            loggersSelect.setValue(wizard.getConfig().getLoggers());
        }
        //restore loggers if back button is pressed
        if (!Util.isCollectionEmpty(wizard.getConfig().getMonitors())) {
            monitorsSelect.setValue(wizard.getConfig().getMonitors());
        }
        //restore tablet servers if back button is pressed
        if (!Util.isCollectionEmpty(wizard.getConfig().getTabletServers())) {
            tabletServers.setValue(wizard.getConfig().getTabletServers());
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
        loggersSelect.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Collection) event.getProperty().getValue());
                    wizard.getConfig().setLoggers(agentList);
                }
            }
        });
        //add value change handler
        monitorsSelect.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Collection) event.getProperty().getValue());
                    wizard.getConfig().setMonitors(agentList);
                }
            }
        });
        //add value change handler
        tabletServers.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    Set<Agent> agentList = new HashSet((Collection) event.getProperty().getValue());
                    wizard.getConfig().setTabletServers(agentList);
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
                } else if (Util.isCollectionEmpty(wizard.getConfig().getTracers())) {
                    show("Please, select tracer(s)");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getMonitors())) {
                    show("Please, select monitor(s)");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getLoggers())) {
                    show("Please, select logger(s)");
                } else if (Util.isCollectionEmpty(wizard.getConfig().getTabletServers())) {
                    show("Please, select tablet server(s)");
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
        content.addComponent(tracersSelect, 0, 1, 2, 1);
        content.addComponent(monitorsSelect, 0, 2, 2, 2);
        content.addComponent(loggersSelect, 0, 3, 2, 3);
        content.addComponent(tabletServers, 0, 4, 2, 4);
        content.addComponent(buttons, 0, 5, 2, 5);

        addComponent(layout);

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
                new BeanItemContainer<Agent>(
                        Agent.class, hadoopNodes)
        );

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
