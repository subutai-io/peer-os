/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.accumulo.manager;


import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.accumulo.Config;
import org.safehaus.subutai.api.accumulo.NodeType;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.ui.accumulo.AccumuloUI;
import org.safehaus.subutai.ui.accumulo.common.UiUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author dilshat
 */
public class Manager {

	private final GridLayout contentRoot;
	private final ComboBox clusterCombo;
	private final Table mastersTable;
	private final Table tracersTable;
	private final Table slavesTable;
	private final Pattern masterPattern = Pattern.compile(".*(Master.+?g).*");
	private final Pattern gcPattern = Pattern.compile(".*(GC.+?g).*");
	private final Pattern monitorPattern = Pattern.compile(".*(Monitor.+?g).*");
	private final Pattern tracerPattern = Pattern.compile(".*(Tracer.+?g).*");
	private final Pattern loggerPattern = Pattern.compile(".*(Logger.+?g).*");
	private final Pattern tabletServerPattern = Pattern.compile(".*(Tablet Server.+?g).*");
	private Config config;


	public Manager() {

		contentRoot = new GridLayout();
		contentRoot.setSpacing(true);
		contentRoot.setMargin(true);
		contentRoot.setSizeFull();
		contentRoot.setRows(17);
		contentRoot.setColumns(1);

		//tables go here
		mastersTable = UiUtil.createTableTemplate("Masters", false);
		tracersTable = UiUtil.createTableTemplate("Tracers", true);
		slavesTable = UiUtil.createTableTemplate("Slaves", true);
		//tables go here

		HorizontalLayout controlsContent = new HorizontalLayout();
		controlsContent.setSpacing(true);

		Label clusterNameLabel = new Label("Select the cluster");
		controlsContent.addComponent(clusterNameLabel);

		clusterCombo = new ComboBox();
		clusterCombo.setImmediate(true);
		clusterCombo.setTextInputAllowed(false);
		clusterCombo.setWidth(200, Sizeable.Unit.PIXELS);
		clusterCombo.addValueChangeListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				config = (Config) event.getProperty().getValue();
				refreshUI();
			}
		});
		controlsContent.addComponent(clusterCombo);

		Button refreshClustersBtn = new Button("Refresh clusters");
		refreshClustersBtn.addStyleName("default");
		refreshClustersBtn.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				refreshClustersInfo();
			}
		});
		controlsContent.addComponent(refreshClustersBtn);

		Button checkAllBtn = new Button("Check all");
		checkAllBtn.addStyleName("default");
		checkAllBtn.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				checkNodesStatus(mastersTable);
				checkNodesStatus(slavesTable);
				checkNodesStatus(tracersTable);
			}
		});
		controlsContent.addComponent(checkAllBtn);

		Button startClusterBtn = new Button("Start cluster");
		startClusterBtn.addStyleName("default");
		startClusterBtn.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				UUID trackID = AccumuloUI.getAccumuloManager().startCluster(config.getClusterName());
				ProgressWindow window = new ProgressWindow(AccumuloUI.getExecutor(), AccumuloUI.getTracker(), trackID,
						Config.PRODUCT_KEY);
				window.getWindow().addCloseListener(new Window.CloseListener() {
					@Override
					public void windowClose(Window.CloseEvent closeEvent) {
						refreshClustersInfo();
					}
				});
				contentRoot.getUI().addWindow(window.getWindow());
			}
		});
		controlsContent.addComponent(startClusterBtn);

		Button stopClusterBtn = new Button("Stop cluster");
		stopClusterBtn.addStyleName("default");
		stopClusterBtn.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				UUID trackID = AccumuloUI.getAccumuloManager().stopCluster(config.getClusterName());
				ProgressWindow window = new ProgressWindow(AccumuloUI.getExecutor(), AccumuloUI.getTracker(), trackID,
						Config.PRODUCT_KEY);
				window.getWindow().addCloseListener(new Window.CloseListener() {
					@Override
					public void windowClose(Window.CloseEvent closeEvent) {
						refreshClustersInfo();
					}
				});
				contentRoot.getUI().addWindow(window.getWindow());
			}
		});
		controlsContent.addComponent(stopClusterBtn);

		Button destroyClusterBtn = new Button("Destroy cluster");
		destroyClusterBtn.addStyleName("default");
		destroyClusterBtn.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				if (config != null) {
					ConfirmationDialog alert = new ConfirmationDialog(
							String.format("Do you want to destroy the %s cluster?", config.getClusterName()), "Yes",
							"No");
					alert.getOk().addClickListener(new Button.ClickListener() {
						@Override
						public void buttonClick(Button.ClickEvent clickEvent) {
							UUID trackID = AccumuloUI.getAccumuloManager().uninstallCluster(config.getClusterName());
							ProgressWindow window =
									new ProgressWindow(AccumuloUI.getExecutor(), AccumuloUI.getTracker(), trackID,
											Config.PRODUCT_KEY);
							window.getWindow().addCloseListener(new Window.CloseListener() {
								@Override
								public void windowClose(Window.CloseEvent closeEvent) {
									refreshClustersInfo();
								}
							});
							contentRoot.getUI().addWindow(window.getWindow());
						}
					});
					contentRoot.getUI().addWindow(alert.getAlert());
				} else {
					Notification.show("Please, select cluster");
				}
			}
		});
		controlsContent.addComponent(destroyClusterBtn);

		Button addTracerBtn = new Button("Add Tracer");
		addTracerBtn.addStyleName("default");
		addTracerBtn.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				if (config != null) {

					org.safehaus.subutai.api.hadoop.Config hadoopConfig =
							AccumuloUI.getHadoopManager().getCluster(config.getClusterName());
					org.safehaus.subutai.api.zookeeper.Config zkConfig =
							AccumuloUI.getZookeeperManager().getCluster(config.getClusterName());
					if (hadoopConfig != null) {
						if (zkConfig != null) {
							Set<Agent> availableNodes = new HashSet<>(hadoopConfig.getAllNodes());
							availableNodes.retainAll(zkConfig.getNodes());
							availableNodes.removeAll(config.getTracers());
							if (availableNodes.isEmpty()) {
								Notification.show("All Hadoop nodes already have tracers installed");
								return;
							}

							AddNodeWindow addNodeWindow = new AddNodeWindow(config, availableNodes, NodeType.TRACER);
							contentRoot.getUI().addWindow(addNodeWindow);
							addNodeWindow.addCloseListener(new Window.CloseListener() {
								@Override
								public void windowClose(Window.CloseEvent closeEvent) {
									refreshClustersInfo();
								}
							});
						} else {
							Notification
									.show(String.format("Zookeeper cluster %s not found", config.getClusterName()));
						}
					} else {
						Notification.show(String.format("Hadoop cluster %s not found", config.getClusterName()));
					}
				} else {
					Notification.show("Please, select cluster");
				}
			}
		});
		controlsContent.addComponent(addTracerBtn);

		Button addSlaveBtn = new Button("Add Slave");
		addSlaveBtn.addStyleName("default");
		addSlaveBtn.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				if (config != null) {

					org.safehaus.subutai.api.hadoop.Config hadoopConfig =
							AccumuloUI.getHadoopManager().getCluster(config.getClusterName());
					org.safehaus.subutai.api.zookeeper.Config zkConfig =
							AccumuloUI.getZookeeperManager().getCluster(config.getClusterName());
					if (hadoopConfig != null) {
						if (zkConfig != null) {
							Set<Agent> availableNodes = new HashSet<>(hadoopConfig.getAllNodes());
							availableNodes.retainAll(zkConfig.getNodes());
							availableNodes.removeAll(config.getSlaves());
							if (availableNodes.isEmpty()) {
								Notification.show("All Hadoop nodes already have slaves installed");
								return;
							}

							AddNodeWindow addNodeWindow = new AddNodeWindow(config, availableNodes, NodeType.LOGGER);
							contentRoot.getUI().addWindow(addNodeWindow);
							addNodeWindow.addCloseListener(new Window.CloseListener() {
								@Override
								public void windowClose(Window.CloseEvent closeEvent) {
									refreshClustersInfo();
								}
							});
						} else {
							Notification
									.show(String.format("Zookeeper cluster %s not found", config.getClusterName()));
						}
					} else {
						Notification.show(String.format("Hadoop cluster %s not found", config.getClusterName()));
					}
				} else {
					Notification.show("Please, select cluster");
				}
			}
		});
		controlsContent.addComponent(addSlaveBtn);

		HorizontalLayout customPropertyContent = new HorizontalLayout();
		customPropertyContent.setSpacing(true);

		Label propertyNameLabel = new Label("Property Name");
		customPropertyContent.addComponent(propertyNameLabel);
		final TextField propertyNameTextField = new TextField();
		customPropertyContent.addComponent(propertyNameTextField);

		Button removePropertyBtn = new Button("Remove");
		removePropertyBtn.addStyleName("default");
		removePropertyBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				if (config != null) {
					String propertyName = (String) propertyNameTextField.getValue();
					if (Strings.isNullOrEmpty(propertyName)) {
						Notification.show("Please, specify property name to remove");
					} else {
						UUID trackID =
								AccumuloUI.getAccumuloManager().removeProperty(config.getClusterName(), propertyName);

						ProgressWindow window =
								new ProgressWindow(AccumuloUI.getExecutor(), AccumuloUI.getTracker(), trackID,
										Config.PRODUCT_KEY);
						window.getWindow().addCloseListener(new Window.CloseListener() {
							@Override
							public void windowClose(Window.CloseEvent closeEvent) {
								refreshClustersInfo();
							}
						});
						contentRoot.getUI().addWindow(window.getWindow());
					}
				} else {
					Notification.show("Please, select cluster");
				}
			}
		});
		customPropertyContent.addComponent(removePropertyBtn);

		Label propertyValueLabel = new Label("Property Value");
		customPropertyContent.addComponent(propertyValueLabel);
		final TextField propertyValueTextField = new TextField();
		customPropertyContent.addComponent(propertyValueTextField);
		Button addPropertyBtn = new Button("Add");
		addPropertyBtn.addStyleName("default");
		addPropertyBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				if (config != null) {
					String propertyName = propertyNameTextField.getValue();
					String propertyValue = propertyValueTextField.getValue();
					if (Strings.isNullOrEmpty(propertyName)) {
						Notification.show("Please, specify property name to add");
					} else if (Strings.isNullOrEmpty(propertyValue)) {
						Notification.show("Please, specify property name to set");
					} else {
						UUID trackID = AccumuloUI.getAccumuloManager()
								.addProperty(config.getClusterName(), propertyName, propertyValue);

						ProgressWindow window =
								new ProgressWindow(AccumuloUI.getExecutor(), AccumuloUI.getTracker(), trackID,
										Config.PRODUCT_KEY);
						window.getWindow().addCloseListener(new Window.CloseListener() {
							@Override
							public void windowClose(Window.CloseEvent closeEvent) {
								refreshClustersInfo();
							}
						});
						contentRoot.getUI().addWindow(window.getWindow());
					}
				} else {
					Notification.show("Please, select cluster");
				}
			}
		});
		customPropertyContent.addComponent(addPropertyBtn);

		contentRoot.addComponent(controlsContent, 0, 0);
		contentRoot.addComponent(customPropertyContent, 0, 1);
		contentRoot.addComponent(mastersTable, 0, 2, 0, 6);
		contentRoot.addComponent(tracersTable, 0, 7, 0, 11);
		contentRoot.addComponent(slavesTable, 0, 12, 0, 16);
	}


	private void refreshUI() {
		if (config != null) {
			populateTable(slavesTable, new ArrayList<>(config.getSlaves()), false);
			populateTable(tracersTable, new ArrayList<>(config.getTracers()), false);
			List<Agent> masters = new ArrayList<>();
			masters.add(config.getMasterNode());
			masters.add(config.getGcNode());
			masters.add(config.getMonitor());
			populateTable(mastersTable, masters, true);
		} else {
			slavesTable.removeAllItems();
			tracersTable.removeAllItems();
			mastersTable.removeAllItems();
		}
	}


	public void refreshClustersInfo() {
		List<Config> mongoClusterInfos = AccumuloUI.getAccumuloManager().getClusters();
		Config clusterInfo = (Config) clusterCombo.getValue();
		clusterCombo.removeAllItems();
		if (mongoClusterInfos != null && mongoClusterInfos.size() > 0) {
			for (Config mongoClusterInfo : mongoClusterInfos) {
				clusterCombo.addItem(mongoClusterInfo);
				clusterCombo.setItemCaption(mongoClusterInfo, mongoClusterInfo.getClusterName());
			}
			if (clusterInfo != null) {
				for (Config mongoClusterInfo : mongoClusterInfos) {
					if (mongoClusterInfo.getClusterName().equals(clusterInfo.getClusterName())) {
						clusterCombo.setValue(mongoClusterInfo);
						return;
					}
				}
			} else {
				clusterCombo.setValue(mongoClusterInfos.iterator().next());
			}
		}
	}


	public static void checkNodesStatus(Table table) {
		UiUtil.clickAllButtonsInTable(table, "Check");
	}


	private void populateTable(final Table table, List<Agent> agents, final boolean masters) {

		table.removeAllItems();

		int i = 0;
		for (final Agent agent : agents) {
			i++;
			final Button checkBtn = new Button("Check");
			final Button destroyBtn = new Button("Destroy");
			final Embedded progressIcon = new Embedded("", new ThemeResource("img/spinner.gif"));
			final Label resultHolder = new Label();
			destroyBtn.setEnabled(false);
			progressIcon.setVisible(false);

			table.addItem(masters ? new Object[] {
					(i == 1 ? UiUtil.MASTER_PREFIX : i == 2 ? UiUtil.GC_PREFIX : UiUtil.MONITOR_PREFIX)
							+ agent.getHostname(), checkBtn, resultHolder, progressIcon
			} : new Object[] {
					agent.getHostname(), checkBtn, destroyBtn, resultHolder, progressIcon
			}, null);

			checkBtn.addClickListener(new Button.ClickListener() {

				@Override
				public void buttonClick(Button.ClickEvent event) {
					progressIcon.setVisible(true);

					AccumuloUI.getExecutor().execute(
							new CheckTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

								public void onComplete(String result) {
									synchronized (progressIcon) {
										if (masters) {
											resultHolder.setValue(parseMastersState(result));
										} else if (table == tracersTable) {
											resultHolder.setValue(parseTracersState(result));
										} else if (table == slavesTable) {
											resultHolder.setValue(parseSlavesState(result));
										}
										destroyBtn.setEnabled(true);
										progressIcon.setVisible(false);
									}
								}
							}));
				}
			});


			destroyBtn.addClickListener(new Button.ClickListener() {

				@Override
				public void buttonClick(Button.ClickEvent event) {

					ConfirmationDialog alert = new ConfirmationDialog(
							String.format("Do you want to destroy the %s node?", agent.getHostname()), "Yes", "No");
					alert.getOk().addClickListener(new Button.ClickListener() {
						@Override
						public void buttonClick(Button.ClickEvent clickEvent) {
							UUID trackID = AccumuloUI.getAccumuloManager()
									.destroyNode(config.getClusterName(), agent.getHostname(),
											table == tracersTable ? NodeType.TRACER :
													NodeType.LOGGER);

							ProgressWindow window =
									new ProgressWindow(AccumuloUI.getExecutor(), AccumuloUI.getTracker(), trackID,
											Config.PRODUCT_KEY);
							window.getWindow().addCloseListener(new Window.CloseListener() {
								@Override
								public void windowClose(Window.CloseEvent closeEvent) {
									refreshClustersInfo();
								}
							});
							contentRoot.getUI().addWindow(window.getWindow());
						}
					});

					contentRoot.getUI().addWindow(alert.getAlert());
				}
			});
		}
	}


	private String parseMastersState(String result) {
		StringBuilder parsedResult = new StringBuilder();
		Matcher masterMatcher = masterPattern.matcher(result);
		if (masterMatcher.find()) {
			parsedResult.append(masterMatcher.group(1)).append(" ");
		}
		Matcher gcMatcher = gcPattern.matcher(result);
		if (gcMatcher.find()) {
			parsedResult.append(gcMatcher.group(1)).append(" ");
		}
		Matcher monitorMatcher = monitorPattern.matcher(result);
		if (monitorMatcher.find()) {
			parsedResult.append(monitorMatcher.group(1)).append(" ");
		}

		return parsedResult.toString();
	}


	private String parseTracersState(String result) {
		StringBuilder parsedResult = new StringBuilder();
		Matcher tracersMatcher = tracerPattern.matcher(result);
		if (tracersMatcher.find()) {
			parsedResult.append(tracersMatcher.group(1)).append(" ");
		}

		return parsedResult.toString();
	}


	private String parseSlavesState(String result) {
		StringBuilder parsedResult = new StringBuilder();
		Matcher loggersMatcher = loggerPattern.matcher(result);
		if (loggersMatcher.find()) {
			parsedResult.append(loggersMatcher.group(1)).append(" ");
		}
		Matcher tablerServersMatcher = tabletServerPattern.matcher(result);
		if (tablerServersMatcher.find()) {
			parsedResult.append(tablerServersMatcher.group(1)).append(" ");
		}

		return parsedResult.toString();
	}


	public Component getContent() {
		return contentRoot;
	}
}
