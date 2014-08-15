/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.presto.manager;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.presto.Config;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.CompleteEvent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.shared.protocol.enums.NodeState;
import org.safehaus.subutai.ui.presto.PrestoUI;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author dilshat
 */
public class Manager {

	private final GridLayout contentRoot;
	private final ComboBox clusterCombo;
	private final Table nodesTable;
	private final String COORDINATOR_PREFIX = "Coordinator: ";
	private Config config;

	public Manager() {

		contentRoot = new GridLayout();
		contentRoot.setSpacing(true);
		contentRoot.setMargin(true);
		contentRoot.setSizeFull();
		contentRoot.setRows(10);
		contentRoot.setColumns(1);

		//tables go here
		nodesTable = createTableTemplate("Nodes");
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
			public void buttonClick(Button.ClickEvent clickEvent) {
				refreshClustersInfo();
			}
		});
		controlsContent.addComponent(refreshClustersBtn);

		Button checkAllBtn = new Button("Check All");
		checkAllBtn.addStyleName("default");
		checkAllBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				checkAllNodes();
			}
		});
		controlsContent.addComponent(checkAllBtn);

		Button startAllBtn = new Button("Start All");
		startAllBtn.addStyleName("default");
		startAllBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				startAllNodes();
			}
		});
		controlsContent.addComponent(startAllBtn);

		Button stopAllBtn = new Button("Stop All");
		stopAllBtn.addStyleName("default");
		stopAllBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				stopAllNodes();
			}
		});
		controlsContent.addComponent(stopAllBtn);

		Button destroyClusterBtn = new Button("Destroy cluster");
		destroyClusterBtn.addStyleName("default");
		destroyClusterBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				if (config != null) {
					ConfirmationDialog alert = new ConfirmationDialog(String.format("Do you want to destroy the %s cluster?", config.getClusterName()),
							"Yes", "No");
					alert.getOk().addClickListener(new Button.ClickListener() {
						@Override
						public void buttonClick(Button.ClickEvent clickEvent) {
							UUID trackID = PrestoUI.getPrestoManager().uninstallCluster(config.getClusterName());
							ProgressWindow window = new ProgressWindow(PrestoUI.getExecutor(), PrestoUI.getTracker(), trackID, Config.PRODUCT_KEY);
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
					show("Please, select cluster");
				}
			}
		});

		controlsContent.addComponent(destroyClusterBtn);

		Button addNodeBtn = new Button("Add Node");
		addNodeBtn.addStyleName("default");
		addNodeBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				if (config != null) {
					org.safehaus.subutai.api.hadoop.Config info = PrestoUI.getHadoopManager().getCluster(config.getClusterName());
					if (info != null) {
						Set<Agent> nodes = new HashSet<>(info.getAllNodes());
						nodes.removeAll(config.getAllNodes());
						if (!nodes.isEmpty()) {
							AddNodeWindow addNodeWindow = new AddNodeWindow(config, nodes);
							contentRoot.getUI().addWindow(addNodeWindow);
							addNodeWindow.addCloseListener(new Window.CloseListener() {
								@Override
								public void windowClose(Window.CloseEvent closeEvent) {
									refreshClustersInfo();
								}
							});
						} else {
							show("All nodes in corresponding Hadoop cluster have Presto installed");
						}
					} else {
						show("Hadoop cluster info not found");
					}
				} else {
					show("Please, select cluster");
				}
			}
		});

		controlsContent.addComponent(addNodeBtn);

		contentRoot.addComponent(controlsContent, 0, 0);
		contentRoot.addComponent(nodesTable, 0, 1, 0, 9);

	}

	public Component getContent() {
		return contentRoot;
	}

	public void checkAllNodes() {
		for (Object o : nodesTable.getItemIds()) {
			int rowId = (Integer) o;
			Item row = nodesTable.getItem(rowId);
			Button checkBtn = (Button) (row.getItemProperty("Check").getValue());
			checkBtn.click();
		}
	}

	public void startAllNodes() {
		for (Object o : nodesTable.getItemIds()) {
			int rowId = (Integer) o;
			Item row = nodesTable.getItem(rowId);
			Button checkBtn = (Button) (row.getItemProperty("Start").getValue());
			checkBtn.click();
		}
	}

	public void stopAllNodes() {
		for (Object o : nodesTable.getItemIds()) {
			int rowId = (Integer) o;
			Item row = nodesTable.getItem(rowId);
			Button checkBtn = (Button) (row.getItemProperty("Stop").getValue());
			checkBtn.click();
		}
	}

	private void show(String notification) {
		Notification.show(notification);
	}

	private void populateTable(final Table table, Set<Agent> workers, final Agent coordinator) {

		table.removeAllItems();

		for (final Agent agent : workers) {
			final Button checkBtn = new Button("Check");
			checkBtn.addStyleName("default");
			final Button startBtn = new Button("Start");
			startBtn.addStyleName("default");
			final Button stopBtn = new Button("Stop");
			stopBtn.addStyleName("default");
			final Button setCoordinatorBtn = new Button("Set As Coordinator");
			setCoordinatorBtn.addStyleName("default");
			final Button destroyBtn = new Button("Destroy");
			destroyBtn.addStyleName("default");
			final Embedded progressIcon = new Embedded("", new ThemeResource("img/spinner.gif"));
			stopBtn.setEnabled(false);
			startBtn.setEnabled(false);
			progressIcon.setVisible(false);

			table.addItem(new Object[] {
							agent.getHostname(),
							checkBtn,
							startBtn,
							stopBtn,
							setCoordinatorBtn,
							destroyBtn,
							progressIcon},
					null
			);

			checkBtn.addClickListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent clickEvent) {
					progressIcon.setVisible(true);
					startBtn.setEnabled(false);
					stopBtn.setEnabled(false);
					setCoordinatorBtn.setEnabled(false);
					destroyBtn.setEnabled(false);

					PrestoUI.getExecutor().execute(new CheckTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

						public void onComplete(NodeState state) {
							synchronized (progressIcon) {
								if (state == NodeState.RUNNING) {
									stopBtn.setEnabled(true);
								} else if (state == NodeState.STOPPED) {
									startBtn.setEnabled(true);
								}
								setCoordinatorBtn.setEnabled(true);
								destroyBtn.setEnabled(true);
								progressIcon.setVisible(false);
							}
						}
					}));
				}
			});

			startBtn.addClickListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent clickEvent) {
					progressIcon.setVisible(true);
					startBtn.setEnabled(false);
					stopBtn.setEnabled(false);
					setCoordinatorBtn.setEnabled(false);
					destroyBtn.setEnabled(false);

					PrestoUI.getExecutor().execute(new StartTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

						public void onComplete(NodeState state) {
							synchronized (progressIcon) {
								if (state == NodeState.RUNNING) {
									stopBtn.setEnabled(true);
								} else if (state == NodeState.STOPPED) {
									startBtn.setEnabled(true);
								}
								setCoordinatorBtn.setEnabled(true);
								destroyBtn.setEnabled(true);
								progressIcon.setVisible(false);
							}
						}
					}));
				}
			});

			stopBtn.addClickListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent clickEvent) {
					progressIcon.setVisible(true);
					startBtn.setEnabled(false);
					stopBtn.setEnabled(false);
					setCoordinatorBtn.setEnabled(false);
					destroyBtn.setEnabled(false);

					PrestoUI.getExecutor().execute(new StopTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

						public void onComplete(NodeState state) {
							synchronized (progressIcon) {
								if (state == NodeState.RUNNING) {
									stopBtn.setEnabled(true);
								} else if (state == NodeState.STOPPED) {
									startBtn.setEnabled(true);
								}
								setCoordinatorBtn.setEnabled(true);
								destroyBtn.setEnabled(true);
								progressIcon.setVisible(false);
							}
						}
					}));
				}
			});

			setCoordinatorBtn.addClickListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent clickEvent) {
					ConfirmationDialog alert = new ConfirmationDialog(String.format("Do you want to set %s as coordinator node?", agent.getHostname()),
							"Yes", "No");
					alert.getOk().addClickListener(new Button.ClickListener() {
						@Override
						public void buttonClick(Button.ClickEvent clickEvent) {
							UUID trackID = PrestoUI.getPrestoManager().changeCoordinatorNode(config.getClusterName(), agent.getHostname());
							ProgressWindow window = new ProgressWindow(PrestoUI.getExecutor(), PrestoUI.getTracker(), trackID, Config.PRODUCT_KEY);
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

			destroyBtn.addClickListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent clickEvent) {
					ConfirmationDialog alert = new ConfirmationDialog(String.format("Do you want to destroy the %s node?", agent.getHostname()),
							"Yes", "No");
					alert.getOk().addClickListener(new Button.ClickListener() {
						@Override
						public void buttonClick(Button.ClickEvent clickEvent) {
							UUID trackID = PrestoUI.getPrestoManager().destroyWorkerNode(config.getClusterName(), agent.getHostname());
							ProgressWindow window = new ProgressWindow(PrestoUI.getExecutor(), PrestoUI.getTracker(), trackID, Config.PRODUCT_KEY);
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

		//add master here
		final Button checkBtn = new Button("Check");
		checkBtn.addStyleName("default");
		final Button startBtn = new Button("Start");
		startBtn.addStyleName("default");
		final Button stopBtn = new Button("Stop");
		stopBtn.addStyleName("default");
		final Embedded progressIcon = new Embedded("", new ThemeResource("img/spinner.gif"));
		stopBtn.setEnabled(false);
		startBtn.setEnabled(false);
		progressIcon.setVisible(false);

		table.addItem(new Object[] {
						COORDINATOR_PREFIX + coordinator.getHostname(),
						checkBtn,
						startBtn,
						stopBtn,
						null,
						null,
						progressIcon},
				null
		);

		checkBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				progressIcon.setVisible(true);
				startBtn.setEnabled(false);
				stopBtn.setEnabled(false);

				PrestoUI.getExecutor().execute(new CheckTask(config.getClusterName(), coordinator.getHostname(), new CompleteEvent() {

					public void onComplete(NodeState state) {
						synchronized (progressIcon) {
							if (state == NodeState.RUNNING) {
								stopBtn.setEnabled(true);
							} else if (state == NodeState.STOPPED) {
								startBtn.setEnabled(true);
							}
							progressIcon.setVisible(false);
						}
					}
				}));
			}
		});

		startBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				progressIcon.setVisible(true);
				startBtn.setEnabled(false);
				stopBtn.setEnabled(false);

				PrestoUI.getExecutor().execute(new StartTask(config.getClusterName(), coordinator.getHostname(), new CompleteEvent() {

					public void onComplete(NodeState state) {
						synchronized (progressIcon) {
							if (state == NodeState.RUNNING) {
								stopBtn.setEnabled(true);
							} else if (state == NodeState.STOPPED) {
								startBtn.setEnabled(true);
							}
							progressIcon.setVisible(false);
						}
					}
				}));
			}
		});

		stopBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				progressIcon.setVisible(true);
				startBtn.setEnabled(false);
				stopBtn.setEnabled(false);

				PrestoUI.getExecutor().execute(new StopTask(config.getClusterName(), coordinator.getHostname(), new CompleteEvent() {

					public void onComplete(NodeState state) {
						synchronized (progressIcon) {
							if (state == NodeState.RUNNING) {
								stopBtn.setEnabled(true);
							} else if (state == NodeState.STOPPED) {
								startBtn.setEnabled(true);
							}
							progressIcon.setVisible(false);
						}
					}
				}));
			}
		});
	}

	private void refreshUI() {
		if (config != null) {
			populateTable(nodesTable, config.getWorkers(), config.getCoordinatorNode());
		} else {
			nodesTable.removeAllItems();
		}
	}

	public void refreshClustersInfo() {
		List<Config> clustersInfo = PrestoUI.getPrestoManager().getClusters();
		Config clusterInfo = (Config) clusterCombo.getValue();
		clusterCombo.removeAllItems();
		if (clustersInfo != null && clustersInfo.size() > 0) {
			for (Config mongoClusterInfo : clustersInfo) {
				clusterCombo.addItem(mongoClusterInfo);
				clusterCombo.setItemCaption(mongoClusterInfo,
						mongoClusterInfo.getClusterName());
			}
			if (clusterInfo != null) {
				for (Config mongoClusterInfo : clustersInfo) {
					if (mongoClusterInfo.getClusterName().equals(clusterInfo.getClusterName())) {
						clusterCombo.setValue(mongoClusterInfo);
						return;
					}
				}
			} else {
				clusterCombo.setValue(clustersInfo.iterator().next());
			}
		}
	}

	private Table createTableTemplate(String caption) {
		final Table table = new Table(caption);
		table.addContainerProperty("Host", String.class, null);
		table.addContainerProperty("Check", Button.class, null);
		table.addContainerProperty("Start", Button.class, null);
		table.addContainerProperty("Stop", Button.class, null);
		table.addContainerProperty("Action", Button.class, null);
		table.addContainerProperty("Destroy", Button.class, null);
		table.addContainerProperty("Status", Embedded.class, null);
		table.setSizeFull();
		table.setPageLength(10);
		table.setSelectable(false);
		table.setImmediate(true);

		table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.isDoubleClick()) {
					String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
					Agent lxcAgent = PrestoUI.getAgentManager().getAgentByHostname(lxcHostname.replaceAll("Coordinator: ", ""));
					if (lxcAgent != null) {
						TerminalWindow terminal = new TerminalWindow(Util.wrapAgentToSet(lxcAgent), PrestoUI.getExecutor(), PrestoUI.getCommandRunner(), PrestoUI.getAgentManager());
						contentRoot.getUI().addWindow(terminal.getWindow());
					} else {
						show("Agent is not connected");
					}
				}
			}
		});
		return table;
	}

}
