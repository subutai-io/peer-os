/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.cassandra.ui.manager;


import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;
import org.safehaus.subutai.plugin.cassandra.ui.CassandraUI;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.collect.Sets;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


/**
 * @author dilshat
 */
public class Manager {

	private final Table nodesTable;
	private GridLayout contentRoot;
	private ComboBox clusterCombo;
	private HorizontalLayout controlsContent;
	private CassandraConfig config;


	public Manager() {

		contentRoot = new GridLayout();
		contentRoot.setSpacing(true);
		contentRoot.setMargin(true);
		contentRoot.setSizeFull();
		contentRoot.setRows(10);
		contentRoot.setColumns(1);

		//tables go here
		nodesTable = createTableTemplate("Cluster nodes");

		controlsContent = new HorizontalLayout();
		controlsContent.setSpacing(true);
		controlsContent.setHeight(100, Sizeable.Unit.PERCENTAGE);

		getClusterNameLabel();
		getClusterCombo();
		getRefreshClusterButton();
		getCheckAllButton();
		getStartAllButton();
		getStopAllButton();
		getDestroyClusterButton();

		contentRoot.addComponent(controlsContent, 0, 0);
		contentRoot.addComponent(nodesTable, 0, 1, 0, 9);

	}

	private Table createTableTemplate(String caption) {
		final Table table = new Table(caption);
		table.addContainerProperty("Host", String.class, null);
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
					Agent lxcAgent = CassandraUI.getAgentManager().getAgentByHostname(lxcHostname);
					if (lxcAgent != null) {
						TerminalWindow terminal = new TerminalWindow(Sets.newHashSet(lxcAgent),
                                CassandraUI.getExecutor(),
                                CassandraUI.getCommandRunner(),
                                CassandraUI.getAgentManager());
						contentRoot.getUI().addWindow(terminal.getWindow());
					} else {
						show("Agent is not connected");
					}
				}
			}
		});
		return table;
	}

	private void getClusterNameLabel() {
		Label clusterNameLabel = new Label("Select the cluster");
		controlsContent.addComponent(clusterNameLabel);
		controlsContent.setComponentAlignment(clusterNameLabel, Alignment.MIDDLE_CENTER);
	}

	private void getClusterCombo() {
		clusterCombo = new ComboBox();
		clusterCombo.setImmediate(true);
		clusterCombo.setTextInputAllowed(false);
		clusterCombo.setWidth(200, Sizeable.Unit.PIXELS);
		clusterCombo.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				config = (CassandraConfig) event.getProperty().getValue();
				refreshUI();
			}
		});

		controlsContent.addComponent(clusterCombo);
		controlsContent.setComponentAlignment(clusterCombo, Alignment.MIDDLE_CENTER);
	}

	private void getRefreshClusterButton() {
		Button refreshClustersBtn = new Button("Refresh clusters");
		refreshClustersBtn.addStyleName("default");
		refreshClustersBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				refreshClustersInfo();
			}
		});

		controlsContent.addComponent(refreshClustersBtn);
		controlsContent.setComponentAlignment(refreshClustersBtn, Alignment.MIDDLE_CENTER);
	}

	private void getCheckAllButton() {
		Button checkAllBtn = new Button("Check all");
		checkAllBtn.addStyleName("default");
		checkAllBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				UUID trackID = CassandraUI.getCassandraManager().checkCluster(config.getClusterName());
				ProgressWindow window = new ProgressWindow(CassandraUI.getExecutor(), CassandraUI.getTracker(), trackID, CassandraConfig.PRODUCT_KEY);
				window.getWindow().addCloseListener(new Window.CloseListener() {
					@Override
					public void windowClose(Window.CloseEvent closeEvent) {
						refreshClustersInfo();
					}
				});
				contentRoot.getUI().addWindow(window.getWindow());
			}
		});

		controlsContent.addComponent(checkAllBtn);
		controlsContent.setComponentAlignment(checkAllBtn, Alignment.MIDDLE_CENTER);
	}

	private void getStartAllButton() {
		Button startAllBtn = new Button("Start all");
		startAllBtn.addStyleName("default");
		startAllBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				UUID trackID = CassandraUI.getCassandraManager().startCluster(config.getClusterName());
				ProgressWindow window = new ProgressWindow(CassandraUI.getExecutor(), CassandraUI.getTracker(), trackID, CassandraConfig.PRODUCT_KEY);
				window.getWindow().addCloseListener(new Window.CloseListener() {
					@Override
					public void windowClose(Window.CloseEvent closeEvent) {
						refreshClustersInfo();
					}
				});
				contentRoot.getUI().addWindow(window.getWindow());
			}
		});

		controlsContent.addComponent(startAllBtn);
		controlsContent.setComponentAlignment(startAllBtn, Alignment.MIDDLE_CENTER);
	}

	private void getStopAllButton() {
		Button stopAllBtn = new Button("Stop all");
		stopAllBtn.addStyleName("default");
		stopAllBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				UUID trackID = CassandraUI.getCassandraManager().stopCluster(config.getClusterName());
				ProgressWindow window = new ProgressWindow(CassandraUI.getExecutor(), CassandraUI.getTracker(), trackID, CassandraConfig.PRODUCT_KEY);
				window.getWindow().addCloseListener(new Window.CloseListener() {
					@Override
					public void windowClose(Window.CloseEvent closeEvent) {
						refreshClustersInfo();
					}
				});
				contentRoot.getUI().addWindow(window.getWindow());
			}
		});

		controlsContent.addComponent(stopAllBtn);
		controlsContent.setComponentAlignment(stopAllBtn, Alignment.MIDDLE_CENTER);
	}

	private void getDestroyClusterButton() {
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
							UUID trackID = CassandraUI.getCassandraManager()
									.uninstallCluster(config.getClusterName());

							ProgressWindow window = new ProgressWindow(CassandraUI.getExecutor(), CassandraUI.getTracker(), trackID, CassandraConfig.PRODUCT_KEY);
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
		controlsContent.setComponentAlignment(destroyClusterBtn, Alignment.MIDDLE_CENTER);
	}

	private void show(String notification) {
		Notification.show(notification);
	}

	private void refreshUI() {
		if (config != null) {
			populateTable(nodesTable, config.getNodes());
		} else {
			nodesTable.removeAllItems();
		}
	}

	public void refreshClustersInfo() {
		List<CassandraConfig> info = CassandraUI.getCassandraManager().getClusters();
        CassandraConfig clusterInfo = (CassandraConfig) clusterCombo.getValue();
		clusterCombo.removeAllItems();
		if (info != null && info.size() > 0) {
			for (CassandraConfig mongoInfo : info) {
				clusterCombo.addItem(mongoInfo);
				clusterCombo.setItemCaption(mongoInfo,
						mongoInfo.getClusterName());
			}
			if (clusterInfo != null) {
				for (CassandraConfig cassandraInfo : info) {
					if (cassandraInfo.getClusterName().equals(clusterInfo.getClusterName())) {
						clusterCombo.setValue(cassandraInfo);
						return;
					}
				}
			} else {
				clusterCombo.setValue(info.iterator().next());
			}
		}
	}

	private void populateTable(final Table table, Set<Agent> agents) {
		table.removeAllItems();
		for (Iterator it = agents.iterator(); it.hasNext(); ) {
			final Agent agent = (Agent) it.next();
			final Embedded progressIcon = new Embedded("",
					new ThemeResource("img/spinner.gif"));
			progressIcon.setVisible(false);
			final Object rowId = table.addItem(new Object[] {
							agent.getHostname(),
							progressIcon},
					null
			);
		}
	}

	public Component getContent() {
		return contentRoot;
	}

}
