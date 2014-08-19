/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.ui.wizard;


import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import org.safehaus.subutai.plugin.accumulo.ui.AccumuloUI;
import org.safehaus.subutai.plugin.accumulo.ui.common.UiUtil;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.*;


/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel {
	final Property.ValueChangeListener masterNodeComboChangeListener;
	final Property.ValueChangeListener gcNodeComboChangeListener;


	public ConfigurationStep(final Wizard wizard) {

		//hadoop combo
		final ComboBox hadoopClustersCombo = UiUtil.getCombo("Hadoop cluster");
		//master nodes
		final ComboBox masterNodeCombo = UiUtil.getCombo("Master node");
		final ComboBox gcNodeCombo = UiUtil.getCombo("GC node");
		final ComboBox monitorNodeCombo = UiUtil.getCombo("Monitor node");
		//accumulo init controls
		TextField instanceNameTxtFld = UiUtil.getTextField("Instance name", "Instance name", 20);
		TextField passwordTxtFld = UiUtil.getTextField("Password", "Password", 20);
		//tracers
		final TwinColSelect tracersSelect =
				UiUtil.getTwinSelect("Tracers", "hostname", "Available Nodes", "Selected Nodes", 4);
		//slave nodes
		final TwinColSelect slavesSelect =
				UiUtil.getTwinSelect("Slaves", "hostname", "Available Nodes", "Selected Nodes", 4);

		//get hadoop clusters from db
		List<HadoopClusterConfig> hadoopClusters = AccumuloUI.getHadoopManager().getClusters();
		final List<ZookeeperClusterConfig> zkClusters = AccumuloUI.getZookeeperManager().getClusters();
		Set<HadoopClusterConfig> filteredHadoopClusters = new HashSet<>();

		//filter out those hadoop clusters which have zk clusters installed on top
		for (HadoopClusterConfig hadoopClusterInfo : hadoopClusters) {
			for (ZookeeperClusterConfig zkClusterInfo : zkClusters) {
				if (hadoopClusterInfo.getClusterName().equals(zkClusterInfo.getClusterName())
						&& zkClusterInfo.getSetupType() == SetupType.OVER_HADOOP) {
					filteredHadoopClusters.add(hadoopClusterInfo);
					break;
				}
			}
		}

		//fill hadoopClustersCombo with hadoop cluster infos
		if (filteredHadoopClusters.size() > 0) {
			for (HadoopClusterConfig hadoopClusterInfo : filteredHadoopClusters) {
				hadoopClustersCombo.addItem(hadoopClusterInfo);
				hadoopClustersCombo.setItemCaption(hadoopClusterInfo, hadoopClusterInfo.getClusterName());
			}
		}

		//try to find hadoop cluster info based on one saved in the configuration
		HadoopClusterConfig info =
				AccumuloUI.getHadoopManager().getCluster(wizard.getAccumuloClusterConfig().getClusterName());

		//select if saved found
		if (info != null) {
			hadoopClustersCombo.setValue(info);
			hadoopClustersCombo.setItemCaption(info, info.getClusterName());
		} else if (filteredHadoopClusters.size() > 0) {
			//select first one if saved not found
			hadoopClustersCombo.setValue(filteredHadoopClusters.iterator().next());
		}


		//fill selection controls with hadoop nodes
		if (hadoopClustersCombo.getValue() != null) {
			HadoopClusterConfig hadoopInfo = (HadoopClusterConfig) hadoopClustersCombo.getValue();

			wizard.getAccumuloClusterConfig().setClusterName(hadoopInfo.getClusterName());

			setComboDS(masterNodeCombo, filterAgents(hadoopInfo, zkClusters));
			setComboDS(gcNodeCombo, filterAgents(hadoopInfo, zkClusters));
			setComboDS(monitorNodeCombo, filterAgents(hadoopInfo, zkClusters));
			setTwinSelectDS(tracersSelect, filterAgents(hadoopInfo, zkClusters));
			setTwinSelectDS(slavesSelect, filterAgents(hadoopInfo, zkClusters));
		}

		//on hadoop cluster change reset all controls and config
		hadoopClustersCombo.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					HadoopClusterConfig hadoopInfo = (HadoopClusterConfig) event.getProperty().getValue();
					setComboDS(masterNodeCombo, filterAgents(hadoopInfo, zkClusters));
					setComboDS(gcNodeCombo, filterAgents(hadoopInfo, zkClusters));
					setComboDS(monitorNodeCombo, filterAgents(hadoopInfo, zkClusters));
					setTwinSelectDS(tracersSelect, filterAgents(hadoopInfo, zkClusters));
					setTwinSelectDS(slavesSelect, filterAgents(hadoopInfo, zkClusters));
					wizard.getAccumuloClusterConfig().reset();
					wizard.getAccumuloClusterConfig().setClusterName(hadoopInfo.getClusterName());
				}
			}
		});

		//restore master node if back button is pressed
		if (wizard.getAccumuloClusterConfig().getMasterNode() != null) {
			masterNodeCombo.setValue(wizard.getAccumuloClusterConfig().getMasterNode());
		}
		//restore gc node if back button is pressed
		if (wizard.getAccumuloClusterConfig().getGcNode() != null) {
			gcNodeCombo.setValue(wizard.getAccumuloClusterConfig().getGcNode());
		}
		//restore monitor node if back button is pressed
		if (wizard.getAccumuloClusterConfig().getMonitor() != null) {
			monitorNodeCombo.setValue(wizard.getAccumuloClusterConfig().getMonitor());
		}

		//add value change handler
		masterNodeComboChangeListener = new Property.ValueChangeListener() {

			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					Agent masterNode = (Agent) event.getProperty().getValue();
					wizard.getAccumuloClusterConfig().setMasterNode(masterNode);
					HadoopClusterConfig hadoopInfo = (HadoopClusterConfig) hadoopClustersCombo.getValue();
					List<Agent> hadoopNodes = filterAgents(hadoopInfo, zkClusters);
					hadoopNodes.remove(masterNode);
					gcNodeCombo.removeValueChangeListener(gcNodeComboChangeListener);
					setComboDS(gcNodeCombo, hadoopNodes);
					if (!masterNode.equals(wizard.getAccumuloClusterConfig().getGcNode())) {
						gcNodeCombo.setValue(wizard.getAccumuloClusterConfig().getGcNode());
					} else {
						wizard.getAccumuloClusterConfig().setGcNode(null);
					}
					gcNodeCombo.addValueChangeListener(gcNodeComboChangeListener);
				}
			}
		};
		masterNodeCombo.addValueChangeListener(masterNodeComboChangeListener);
		//add value change handler
		gcNodeComboChangeListener = new Property.ValueChangeListener() {

			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					Agent gcNode = (Agent) event.getProperty().getValue();
					wizard.getAccumuloClusterConfig().setGcNode(gcNode);
					HadoopClusterConfig hadoopInfo = (HadoopClusterConfig) hadoopClustersCombo.getValue();
					List<Agent> hadoopNodes = filterAgents(hadoopInfo, zkClusters);
					hadoopNodes.remove(gcNode);
					masterNodeCombo.removeValueChangeListener(masterNodeComboChangeListener);
					setComboDS(masterNodeCombo, hadoopNodes);
					if (!gcNode.equals(wizard.getAccumuloClusterConfig().getMasterNode())) {
						masterNodeCombo.setValue(wizard.getAccumuloClusterConfig().getMasterNode());
					} else {
						wizard.getAccumuloClusterConfig().setMasterNode(null);
					}
					masterNodeCombo.addValueChangeListener(masterNodeComboChangeListener);
				}
			}
		};
		gcNodeCombo.addValueChangeListener(gcNodeComboChangeListener);
		//add value change handler
		monitorNodeCombo.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					Agent monitor = (Agent) event.getProperty().getValue();
					wizard.getAccumuloClusterConfig().setMonitor(monitor);
				}
			}
		});

		//restore tracers if back button is pressed
		if (!Util.isCollectionEmpty(wizard.getAccumuloClusterConfig().getTracers())) {
			tracersSelect.setValue(wizard.getAccumuloClusterConfig().getTracers());
		}
		//restore slaves if back button is pressed
		if (!Util.isCollectionEmpty(wizard.getAccumuloClusterConfig().getSlaves())) {
			slavesSelect.setValue(wizard.getAccumuloClusterConfig().getSlaves());
		}


		instanceNameTxtFld.setValue(wizard.getAccumuloClusterConfig().getInstanceName());
		instanceNameTxtFld.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getAccumuloClusterConfig().setInstanceName(event.getProperty().getValue().toString().trim());
			}
		});

		passwordTxtFld.setValue(wizard.getAccumuloClusterConfig().getPassword());
		passwordTxtFld.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getAccumuloClusterConfig().setPassword(event.getProperty().getValue().toString().trim());
			}
		});


		//add value change handler
		tracersSelect.addValueChangeListener(new Property.ValueChangeListener() {

			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					Set<Agent> agentList = new HashSet((Collection) event.getProperty().getValue());
					wizard.getAccumuloClusterConfig().setTracers(agentList);
				}
			}
		});
		//add value change handler
		slavesSelect.addValueChangeListener(new Property.ValueChangeListener() {

			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					Set<Agent> agentList = new HashSet((Collection) event.getProperty().getValue());
					wizard.getAccumuloClusterConfig().setSlaves(agentList);
				}
			}
		});

		Button next = new Button("Next");
		next.addStyleName("default");
		//check valid configuration
		next.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {

				if (Strings.isNullOrEmpty(wizard.getAccumuloClusterConfig().getClusterName())) {
					show("Please, select Hadoop cluster");
				} else if (wizard.getAccumuloClusterConfig().getMasterNode() == null) {
					show("Please, select master node");
				} else if (Strings.isNullOrEmpty(wizard.getAccumuloClusterConfig().getInstanceName())) {
					show("Please, specify instance name");
				} else if (Strings.isNullOrEmpty(wizard.getAccumuloClusterConfig().getPassword())) {
					show("Please, specify password");
				} else if (wizard.getAccumuloClusterConfig().getGcNode() == null) {
					show("Please, select gc node");
				} else if (wizard.getAccumuloClusterConfig().getMonitor() == null) {
					show("Please, select monitor");
				} else if (Util.isCollectionEmpty(wizard.getAccumuloClusterConfig().getTracers())) {
					show("Please, select tracer(s)");
				} else if (Util.isCollectionEmpty(wizard.getAccumuloClusterConfig().getSlaves())) {
					show("Please, select slave(s)");
				} else {
					wizard.next();
				}
			}
		});

		Button back = new Button("Back");
		back.addStyleName("default");
		back.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				wizard.back();
			}
		});


		setSizeFull();

		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);
		content.setMargin(true);

		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.addComponent(new Label("Please, specify installation settings"));
		layout.addComponent(content);

		HorizontalLayout masters = new HorizontalLayout();
		masters.setMargin(new MarginInfo(true, false, false, false));
		masters.setSpacing(true);
		masters.addComponent(hadoopClustersCombo);
		masters.addComponent(masterNodeCombo);
		masters.addComponent(gcNodeCombo);
		masters.addComponent(monitorNodeCombo);

		HorizontalLayout credentials = new HorizontalLayout();
		credentials.setMargin(new MarginInfo(true, false, false, false));
		credentials.setSpacing(true);
		credentials.addComponent(instanceNameTxtFld);
		credentials.addComponent(passwordTxtFld);

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setMargin(new MarginInfo(true, false, false, false));
		buttons.setSpacing(true);
		buttons.addComponent(back);
		buttons.addComponent(next);

		content.addComponent(masters);
		content.addComponent(credentials);
		content.addComponent(tracersSelect);
		content.addComponent(slavesSelect);
		content.addComponent(buttons);

		setContent(layout);
	}


	private void setComboDS(ComboBox target, List<Agent> hadoopNodes) {
		target.removeAllItems();
		target.setValue(null);
		for (Agent agent : hadoopNodes) {
			target.addItem(agent);
			target.setItemCaption(agent, agent.getHostname());
		}
	}


	private List<Agent> filterAgents(HadoopClusterConfig hadoopInfo, List<ZookeeperClusterConfig> zkClusters) {

		List<Agent> filteredAgents = new ArrayList<>();
		ZookeeperClusterConfig zkConfig = null;

		for (ZookeeperClusterConfig zkInfo : zkClusters) {
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


	private void setTwinSelectDS(TwinColSelect target, List<Agent> hadoopNodes) {
		target.setValue(null);
		target.setContainerDataSource(new BeanItemContainer<>(Agent.class, hadoopNodes));
	}


	private void show(String notification) {
		Notification.show(notification);
	}
}
