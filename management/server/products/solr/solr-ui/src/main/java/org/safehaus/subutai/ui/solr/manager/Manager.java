/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.solr.manager;


import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.CompleteEvent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.shared.protocol.enums.NodeState;
import org.safehaus.subutai.ui.solr.SolrUI;

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

		Label clusterNameLabel = new Label("Select the installation");
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

		Button refreshClustersBtn = new Button("Refresh installations");
		refreshClustersBtn.addStyleName("default");
		refreshClustersBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				refreshClustersInfo();
			}
		});

		controlsContent.addComponent(refreshClustersBtn);


		contentRoot.addComponent(controlsContent, 0, 0);
		contentRoot.addComponent(nodesTable, 0, 1, 0, 9);
	}

	private Table createTableTemplate(String caption) {
		final Table table = new Table(caption);
		table.addContainerProperty("Host", String.class, null);
		table.addContainerProperty("Check", Button.class, null);
		table.addContainerProperty("Start", Button.class, null);
		table.addContainerProperty("Stop", Button.class, null);
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
					String lxcHostname =
							(String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
					Agent lxcAgent = SolrUI.getAgentManager().getAgentByHostname(lxcHostname);
					if (lxcAgent != null) {
						TerminalWindow terminal =
								new TerminalWindow(Util.wrapAgentToSet(lxcAgent), SolrUI.getExecutor(),
										SolrUI.getCommandRunner(), SolrUI.getAgentManager());
						contentRoot.getUI().addWindow(terminal.getWindow());
					} else {
						show("Agent is not connected");
					}
				}
			}
		});
		return table;
	}

	private void refreshUI() {
		if (config != null) {
			populateTable(nodesTable, config.getNodes());
		} else {
			nodesTable.removeAllItems();
		}
	}

	public void refreshClustersInfo() {
		List<Config> mongoClusterInfos = SolrUI.getSolrManager().getClusters();
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

	private void show(String notification) {
		Notification.show(notification);
	}

	private void populateTable(final Table table, Set<Agent> agents) {

		table.removeAllItems();

		for (final Agent agent : agents) {
			final Button checkBtn = new Button("Check");
			checkBtn.addStyleName("default");
			final Button startBtn = new Button("Start");
			startBtn.addStyleName("default");
			final Button stopBtn = new Button("Stop");
			stopBtn.addStyleName("default");
			final Button destroyBtn = new Button("Destroy");
			destroyBtn.addStyleName("default");
			final Embedded progressIcon = new Embedded("", new ThemeResource("img/spinner.gif"));
			stopBtn.setEnabled(false);
			startBtn.setEnabled(false);
			progressIcon.setVisible(false);

			table.addItem(new Object[] {
					agent.getHostname(), checkBtn, startBtn, stopBtn, destroyBtn, progressIcon
			}, null);

			checkBtn.addClickListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent clickEvent) {
					progressIcon.setVisible(true);
					startBtn.setEnabled(false);
					stopBtn.setEnabled(false);
					destroyBtn.setEnabled(false);

					SolrUI.getExecutor()
							.execute(new CheckTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

								public void onComplete(NodeState state) {
									synchronized (progressIcon) {
										if (state == NodeState.RUNNING) {
											stopBtn.setEnabled(true);
										} else if (state == NodeState.STOPPED) {
											startBtn.setEnabled(true);
										}
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
					destroyBtn.setEnabled(false);

					SolrUI.getExecutor()
							.execute(new StartTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

								public void onComplete(NodeState state) {
									synchronized (progressIcon) {
										if (state == NodeState.RUNNING) {
											stopBtn.setEnabled(true);
										} else {
											startBtn.setEnabled(true);
										}
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
					destroyBtn.setEnabled(false);

					SolrUI.getExecutor()
							.execute(new StopTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

								public void onComplete(NodeState state) {
									synchronized (progressIcon) {
										if (state == NodeState.STOPPED) {
											startBtn.setEnabled(true);
										} else {
											stopBtn.setEnabled(true);
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
				public void buttonClick(Button.ClickEvent clickEvent) {
					ConfirmationDialog alert = new ConfirmationDialog(
							String.format("Do you want to destroy the %s node?", agent.getHostname()), "Yes", "No");
					alert.getOk().addClickListener(new Button.ClickListener() {
						@Override
						public void buttonClick(Button.ClickEvent clickEvent) {
							UUID trackID =
									SolrUI.getSolrManager().destroyNode(config.getClusterName(), agent.getHostname());
							final ProgressWindow window =
									new ProgressWindow(SolrUI.getExecutor(), SolrUI.getTracker(), trackID,
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

	public Component getContent() {
		return contentRoot;
	}
}
