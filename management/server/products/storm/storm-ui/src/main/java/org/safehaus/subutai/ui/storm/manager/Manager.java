package org.safehaus.subutai.ui.storm.manager;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.storm.Config;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.ui.storm.StormUI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Manager {

	private final GridLayout contentRoot;
	private final ComboBox clusterCombo;
	private final Table masterTable, workersTable;
	private Config config;

	public Manager() {

		contentRoot = new GridLayout();
		contentRoot.setSpacing(true);
		contentRoot.setMargin(true);
		contentRoot.setSizeFull();
		contentRoot.setRows(11);
		contentRoot.setColumns(1);

		//tables go here
		masterTable = createTableTemplate("Master node", true);
		workersTable = createTableTemplate("Workers", false);
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

		Button refreshClustersBtn = new Button("Refresh clusters");
		refreshClustersBtn.addStyleName("default");
		refreshClustersBtn.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				refreshClustersInfo();
			}
		});

		Button destroyClusterBtn = new Button("Destroy cluster");
		destroyClusterBtn.addStyleName("default");
		destroyClusterBtn.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				if (config == null) {
					show("Select cluster");
					return;
				}

				ConfirmationDialog alert = new ConfirmationDialog(String.format("Do you want to destroy the %s cluster?", config.getClusterName()),
						"Yes", "No");
				alert.getOk().addClickListener(new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent clickEvent) {
						destroyClusterHandler();
					}
				});

				contentRoot.getUI().addWindow(alert.getAlert());
			}

		});

		Button addNodeBtn = new Button("Add Node");
		addNodeBtn.addStyleName("default");
		addNodeBtn.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				if (config == null) {
					show("Select cluster");
					return;
				}

				UUID trackId = StormUI.getManager().addNode(config.getClusterName(), null);
				ProgressWindow pw = new ProgressWindow(StormUI.getExecutor(),
						StormUI.getTracker(), trackId, Config.PRODUCT_NAME);
				pw.getWindow().addCloseListener(new Window.CloseListener() {

					@Override
					public void windowClose(Window.CloseEvent e) {
						refreshClustersInfo();
					}
				});
				contentRoot.getUI().addWindow(pw.getWindow());
			}
		});

		controlsContent.addComponent(clusterCombo);
		controlsContent.addComponent(refreshClustersBtn);
		controlsContent.addComponent(destroyClusterBtn);
		controlsContent.addComponent(makeBatchOperationButton("Check all", "Check"));
		controlsContent.addComponent(makeBatchOperationButton("Start all", "Start"));
		controlsContent.addComponent(makeBatchOperationButton("Stop all", "Stop"));
		controlsContent.addComponent(makeBatchOperationButton("Restart all", "Restart"));
		controlsContent.addComponent(addNodeBtn);

		contentRoot.addComponent(controlsContent, 0, 0);
		contentRoot.addComponent(masterTable, 0, 1, 0, 5);
		contentRoot.addComponent(workersTable, 0, 6, 0, 10);

	}

	private Table createTableTemplate(String caption, boolean master) {
		final Table table = new Table(caption);
		table.addContainerProperty("Host", String.class, null);
		table.addContainerProperty("Check", Button.class, null);
		table.addContainerProperty("Start", Button.class, null);
		table.addContainerProperty("Stop", Button.class, null);
		table.addContainerProperty("Restart", Button.class, null);
		if (!master)
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
					Agent lxcAgent = StormUI.getAgentManager().getAgentByHostname(lxcHostname);
					if (lxcAgent != null) {
						TerminalWindow terminal = new TerminalWindow(Util.wrapAgentToSet(lxcAgent), StormUI.getExecutor(), StormUI.getCommandRunner(), StormUI.getAgentManager());
						contentRoot.getUI().addWindow(terminal.getWindow());
					} else
						show("Agent is not connected");
				}
			}
		});
		return table;
	}

	private void refreshUI() {
		if (config != null) {
			populateTable(masterTable, true, config.getNimbus());
			populateTable(workersTable, false, config.getSupervisors().toArray(new Agent[0]));
		} else {
			masterTable.removeAllItems();
			workersTable.removeAllItems();
		}
	}

	public void refreshClustersInfo() {
		Config current = (Config) clusterCombo.getValue();
		clusterCombo.removeAllItems();
		List<Config> clustersInfo = StormUI.getManager().getClusters();
		if (clustersInfo != null && clustersInfo.size() > 0) {
			for (Config ci : clustersInfo) {
				clusterCombo.addItem(ci);
				clusterCombo.setItemCaption(ci, ci.getClusterName());
			}
			if (current != null)
				for (Config ci : clustersInfo) {
					if (ci.getClusterName().equals(current.getClusterName())) {
						clusterCombo.setValue(ci);
						return;
					}
				}
		}
	}

	private void show(String notification) {
		Notification.show(notification);
	}

	private void destroyClusterHandler() {

		UUID trackID = StormUI.getManager().uninstallCluster(config.getClusterName());

		ProgressWindow window = new ProgressWindow(StormUI.getExecutor(), StormUI.getTracker(), trackID, Config.PRODUCT_NAME);
		window.getWindow().addCloseListener(new Window.CloseListener() {
			@Override
			public void windowClose(Window.CloseEvent closeEvent) {
				refreshClustersInfo();
			}
		});
		contentRoot.getUI().addWindow(window.getWindow());
	}

	private Button makeBatchOperationButton(String caption, final String itemProperty) {
		Button btn = new Button(caption);
		btn.addStyleName("default");
		btn.addClickListener(new Button.ClickListener() {

			public void buttonClick(Button.ClickEvent event) {
				Table[] tables = new Table[] {masterTable, workersTable};
				for (Table t : tables) {
					for (Object itemId : t.getItemIds()) {
						Item item = t.getItem(itemId);
						Property p = item.getItemProperty(itemProperty);
						if (p != null && p.getValue() instanceof Button)
							((Button) p.getValue()).click();
					}
				}
			}
		});
		return btn;
	}

	private void populateTable(final Table table, boolean server, Agent... agents) {

		table.removeAllItems();

		for (final Agent agent : agents) {
			final Button checkBtn = new Button("Check");
			checkBtn.addStyleName("default");
			final Button startBtn = new Button("Start");
			startBtn.addStyleName("default");
			final Button stopBtn = new Button("Stop");
			stopBtn.addStyleName("default");
			final Button restartBtn = new Button("Restart");
			restartBtn.addStyleName("default");
			final Button destroyBtn = !server ? new Button("Destroy") : null;
			if (destroyBtn != null) destroyBtn.addStyleName("default");
			final Embedded icon = new Embedded("", new ThemeResource(
					"img/spinner.gif"));

			startBtn.setEnabled(false);
			stopBtn.setEnabled(false);
			restartBtn.setEnabled(false);
			icon.setVisible(false);

			final List<java.io.Serializable> items = new ArrayList<>();
			items.add(agent.getHostname());
			items.add(checkBtn);
			items.add(startBtn);
			items.add(stopBtn);
			items.add(restartBtn);
			if (destroyBtn != null) {
				items.add(destroyBtn);
				destroyBtn.addClickListener(new Button.ClickListener() {

					@Override
					public void buttonClick(Button.ClickEvent event) {

						ConfirmationDialog alert = new ConfirmationDialog(String.format("Do you want to destroy the %s node?", agent.getHostname()),
								"Yes", "No");
						alert.getOk().addClickListener(new Button.ClickListener() {
							@Override
							public void buttonClick(Button.ClickEvent clickEvent) {
								UUID trackID = StormUI.getManager().destroyNode(
										config.getClusterName(),
										agent.getHostname());
								ProgressWindow window = new ProgressWindow(StormUI.getExecutor(), StormUI.getTracker(), trackID, Config.PRODUCT_NAME);
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
			items.add(icon);

			table.addItem(items.toArray(), null);

			checkBtn.addClickListener(new Button.ClickListener() {
				public void buttonClick(Button.ClickEvent event) {
					icon.setVisible(true);
					for (Object e : items) {
						if (e instanceof Button) ((Button) e).setEnabled(false);
					}
					final UUID trackId = StormUI.getManager().statusCheck(
							config.getClusterName(), agent.getHostname());
					StormUI.getExecutor().execute(new Runnable() {

						public void run() {
							ProductOperationView po = null;
							while (po == null || po.getState() == ProductOperationState.RUNNING) {
								po = StormUI.getTracker().getProductOperation(
										Config.PRODUCT_NAME, trackId);
							}
							boolean running = po.getState() == ProductOperationState.SUCCEEDED;
							checkBtn.setEnabled(true);
							startBtn.setEnabled(!running);
							stopBtn.setEnabled(running);
							restartBtn.setEnabled(running);
							if (destroyBtn != null) destroyBtn.setEnabled(true);
							icon.setVisible(false);
						}
					});
				}
			});

			startBtn.addClickListener(new Button.ClickListener() {

				@Override
				public void buttonClick(Button.ClickEvent event) {
					icon.setVisible(true);
					for (Object e : items) {
						if (e instanceof Button) ((Button) e).setEnabled(false);
					}
					final UUID trackId = StormUI.getManager().startNode(
							config.getClusterName(), agent.getHostname());

					StormUI.getExecutor().execute(new Runnable() {

						public void run() {
							ProductOperationView po = null;
							while (po == null || po.getState() == ProductOperationState.RUNNING) {
								po = StormUI.getTracker().getProductOperation(
										Config.PRODUCT_NAME, trackId);
							}
							boolean started = po.getState() == ProductOperationState.SUCCEEDED;
							checkBtn.setEnabled(true);
							startBtn.setEnabled(!started);
							stopBtn.setEnabled(started);
							restartBtn.setEnabled(started);
							if (destroyBtn != null) destroyBtn.setEnabled(true);
							icon.setVisible(false);
						}
					});
				}
			});

			stopBtn.addClickListener(new Button.ClickListener() {

				@Override
				public void buttonClick(Button.ClickEvent event) {
					icon.setVisible(true);
					for (Object e : items) {
						if (e instanceof Button) ((Button) e).setEnabled(false);
					}
					final UUID trackId = StormUI.getManager().stopNode(
							config.getClusterName(), agent.getHostname());

					StormUI.getExecutor().execute(new Runnable() {

						public void run() {
							ProductOperationView po = null;
							while (po == null || po.getState() == ProductOperationState.RUNNING) {
								po = StormUI.getTracker().getProductOperation(
										Config.PRODUCT_NAME, trackId);
							}
							boolean stopped = po.getState() == ProductOperationState.SUCCEEDED;
							checkBtn.setEnabled(true);
							startBtn.setEnabled(stopped);
							stopBtn.setEnabled(!stopped);
							restartBtn.setEnabled(!stopped);
							if (destroyBtn != null) destroyBtn.setEnabled(true);
							icon.setVisible(false);
						}
					});

				}
			});

			restartBtn.addClickListener(new Button.ClickListener() {

				public void buttonClick(Button.ClickEvent event) {
					icon.setVisible(true);
					for (Object e : items) {
						if (e instanceof Button) ((Button) e).setEnabled(false);
					}
					final UUID trackId = StormUI.getManager().restartNode(
							config.getClusterName(), agent.getHostname());

					StormUI.getExecutor().execute(new Runnable() {

						public void run() {
							ProductOperationView po = null;
							while (po == null || po.getState() == ProductOperationState.RUNNING) {
								po = StormUI.getTracker().getProductOperation(
										Config.PRODUCT_NAME, trackId);
							}
							boolean ok = po.getState() == ProductOperationState.SUCCEEDED;
							checkBtn.setEnabled(true);
							startBtn.setEnabled(!ok);
							stopBtn.setEnabled(ok);
							restartBtn.setEnabled(true);
							if (destroyBtn != null) destroyBtn.setEnabled(true);
							icon.setVisible(false);
						}
					});
				}
			});

		}
	}

	public Component getContent() {
		return contentRoot;
	}
}
