package org.safehaus.kiskis.mgmt.ui.hive.manager;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.server.ui.ConfirmationDialogCallback;
import org.safehaus.subutai.server.ui.MgmtApplication;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.hive.HiveUI;

import java.util.*;

public class Manager {

    private final VerticalLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table serverTable, clientsTable;
    private Config config;

    public Manager() {

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing(true);
        contentRoot.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        contentRoot.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        VerticalLayout content = new VerticalLayout();
        content.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        contentRoot.addComponent(content);
        contentRoot.setComponentAlignment(content, Alignment.TOP_CENTER);
        contentRoot.setMargin(true);

        //tables go here
        serverTable = createTableTemplate("Server node", 200, true);
        clientsTable = createTableTemplate("Nodes", 200, false);
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing(true);

        Label clusterNameLabel = new Label("Select the cluster");
        controlsContent.addComponent(clusterNameLabel);

        clusterCombo = new ComboBox();
        clusterCombo.setMultiSelect(false);
        clusterCombo.setImmediate(true);
        clusterCombo.setTextInputAllowed(false);
        clusterCombo.setWidth(200, Sizeable.UNITS_PIXELS);
        clusterCombo.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                config = (Config) event.getProperty().getValue();
                refreshUI();
            }
        });

        Button refreshClustersBtn = new Button("Refresh clusters");
        refreshClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                refreshClustersInfo();
            }
        });

        Button destroyClusterBtn = new Button("Destroy cluster");
        destroyClusterBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config == null) {
                    show("Select cluster");
                    return;
                }
                MgmtApplication.showConfirmationDialog("Destroy cluster",
                        String.format("Cluster '%s' will be destroyed. Continue?",
                                config.getClusterName()), "Yes", "Cancel",
                        new ConfirmationDialogCallback() {

                            public void response(boolean ok) {
                                if (ok) destroyClusterHandler();
                            }
                        }
                );
            }

        });

        Button checkAllBtn = new Button("Check all");
        checkAllBtn.addListener(new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                Table[] tables = new Table[]{serverTable, clientsTable};
                for (Table t : tables) {
                    for (Object itemId : t.getItemIds()) {
                        Item item = t.getItem(itemId);
                        Object e = item.getItemProperty("Check").getValue();
                        if (e instanceof Button) ((Button) e).click();
                    }
                }
            }
        });

        Button addNodeBtn = new Button("Add Node");
        addNodeBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config == null) {
                    show("Select cluster");
                    return;
                }

                org.safehaus.kiskis.mgmt.api.hadoop.Config hci = HiveUI.getHadoopManager().getCluster(
                        config.getClusterName());
                if (hci == null) {
                    show("Hadoop cluster info not found");
                    return;
                }

                Set<Agent> set = new HashSet<Agent>(hci.getAllNodes());
                set.remove(config.getServer());
                set.removeAll(config.getClients());
                if (set.isEmpty()) {
                    show("All nodes in Hadoop cluster have Hive installed");
                    return;
                }

                AddNodeWindow w = new AddNodeWindow(config, set);
                MgmtApplication.addCustomWindow(w);
                w.addListener(new Window.CloseListener() {

                    public void windowClose(Window.CloseEvent e) {
                        refreshClustersInfo();
                    }
                });
            }
        });

        controlsContent.addComponent(clusterCombo);
        controlsContent.addComponent(refreshClustersBtn);
        controlsContent.addComponent(destroyClusterBtn);
        controlsContent.addComponent(checkAllBtn);
        controlsContent.addComponent(addNodeBtn);

        content.addComponent(controlsContent);
        content.addComponent(serverTable);
        content.addComponent(clientsTable);

    }

    public Component getContent() {
        return contentRoot;
    }

    private void show(String notification) {
        contentRoot.getWindow().showNotification(notification);
    }

    private void populateTable(final Table table, boolean server, Agent... agents) {

        table.removeAllItems();

        for (final Agent agent : agents) {
            final Button checkBtn = new Button("Check");
            final Button startBtn = new Button("Start");
            final Button stopBtn = new Button("Stop");
            final Button restartBtn = new Button("Restart");
            final Button destroyBtn = !server ? new Button("Destroy") : null;
            final Embedded icon = new Embedded("", new ThemeResource(
                    "../base/common/img/loading-indicator.gif"));

            startBtn.setEnabled(false);
            stopBtn.setEnabled(false);
            restartBtn.setEnabled(false);
            icon.setVisible(false);

            final List items = new ArrayList();
            items.add(agent.getHostname());
            items.add(checkBtn);
            items.add(startBtn);
            items.add(stopBtn);
            items.add(restartBtn);
            if (destroyBtn != null) {
                items.add(destroyBtn);
                destroyBtn.addListener(new Button.ClickListener() {

                    @Override
                    public void buttonClick(Button.ClickEvent event) {

                        MgmtApplication.showConfirmationDialog(
                                "Destroy node",
                                String.format("Do you want to destroy node %s?", agent.getHostname()),
                                "Yes", "Cancel", new ConfirmationDialogCallback() {

                                    @Override
                                    public void response(boolean ok) {
                                        if (!ok) return;
                                        UUID trackID = HiveUI.getManager().destroyNode(
                                                config.getClusterName(),
                                                agent.getHostname());
                                        MgmtApplication.showProgressWindow(
                                                Config.PRODUCT_KEY, trackID,
                                                new Window.CloseListener() {

                                                    public void windowClose(Window.CloseEvent e) {
                                                        refreshClustersInfo();
                                                    }
                                                }
                                        );
                                    }
                                }
                        );

                    }
                });
            }
            items.add(icon);

            table.addItem(items.toArray(), null);

            checkBtn.addListener(new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    icon.setVisible(true);
                    for (Object e : items) {
                        if (e instanceof Button) ((Button) e).setEnabled(false);
                    }
                    final UUID trackId = HiveUI.getManager().statusCheck(
                            config.getClusterName(), agent.getHostname());
                    HiveUI.getExecutor().execute(new Runnable() {

                        public void run() {
                            ProductOperationView po = null;
                            while (po == null || po.getState() == ProductOperationState.RUNNING) {
                                po = HiveUI.getTracker().getProductOperation(
                                        Config.PRODUCT_KEY, trackId);
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

            startBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    for (Object e : items) {
                        if (e instanceof Button) ((Button) e).setEnabled(false);
                    }
                    final UUID trackId = HiveUI.getManager().startNode(
                            config.getClusterName(), agent.getHostname());

                    MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackId,
                            new Window.CloseListener() {

                                public void windowClose(Window.CloseEvent e) {
                                    ProductOperationView po = HiveUI.getTracker()
                                            .getProductOperation(Config.PRODUCT_KEY, trackId);
                                    boolean started = po.getState() == ProductOperationState.SUCCEEDED;
                                    checkBtn.setEnabled(true);
                                    startBtn.setEnabled(!started);
                                    stopBtn.setEnabled(started);
                                    restartBtn.setEnabled(started);
                                    if (destroyBtn != null) destroyBtn.setEnabled(true);
                                }
                            }
                    );
                }
            });

            stopBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    for (Object e : items) {
                        if (e instanceof Button) ((Button) e).setEnabled(false);
                    }
                    final UUID trackId = HiveUI.getManager().stopNode(
                            config.getClusterName(), agent.getHostname());

                    MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackId,
                            new Window.CloseListener() {

                                public void windowClose(Window.CloseEvent e) {
                                    ProductOperationView po = HiveUI.getTracker()
                                            .getProductOperation(Config.PRODUCT_KEY, trackId);
                                    boolean stopped = po.getState() == ProductOperationState.SUCCEEDED;
                                    checkBtn.setEnabled(true);
                                    startBtn.setEnabled(stopped);
                                    stopBtn.setEnabled(!stopped);
                                    restartBtn.setEnabled(!stopped);
                                    if (destroyBtn != null) destroyBtn.setEnabled(true);
                                }
                            }
                    );
                }
            });

            restartBtn.addListener(new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {
                    for (Object e : items) {
                        if (e instanceof Button) ((Button) e).setEnabled(false);
                    }
                    final UUID trackId = HiveUI.getManager().restartNode(
                            config.getClusterName(), agent.getHostname());

                    MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackId,
                            new Window.CloseListener() {

                                public void windowClose(Window.CloseEvent e) {
                                    ProductOperationView po = HiveUI.getTracker()
                                            .getProductOperation(Config.PRODUCT_KEY, trackId);
                                    boolean ok = po.getState() == ProductOperationState.SUCCEEDED;
                                    checkBtn.setEnabled(true);
                                    startBtn.setEnabled(!ok);
                                    stopBtn.setEnabled(ok);
                                    restartBtn.setEnabled(true);
                                    if (destroyBtn != null) destroyBtn.setEnabled(true);
                                }
                            }
                    );
                }
            });

        }
    }

    private void refreshUI() {
        if (config != null) {
            populateTable(serverTable, true, config.getServer());
            populateTable(clientsTable, false, config.getClients().toArray(new Agent[0]));
        } else {
            serverTable.removeAllItems();
            clientsTable.removeAllItems();
        }
    }

    public void refreshClustersInfo() {
        Config current = (Config) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        List<Config> clustersInfo = HiveUI.getManager().getClusters();
        if (clustersInfo != null && clustersInfo.size() > 0) {
            for (Config ci : clustersInfo) {
                clusterCombo.addItem(ci);
                clusterCombo.setItemCaption(ci, ci.getClusterName());
            }
            if (current != null) {
                for (Config ci : clustersInfo) {
                    if (ci.getClusterName().equals(current.getClusterName())) {
                        clusterCombo.setValue(ci);
                        return;
                    }
                }
            }
        }
    }

    private Table createTableTemplate(String caption, int size, boolean server) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Check", Button.class, null);
        table.addContainerProperty("Start", Button.class, null);
        table.addContainerProperty("Stop", Button.class, null);
        table.addContainerProperty("Restart", Button.class, null);
        if (!server)
            table.addContainerProperty("Destroy", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);

        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);

        table.addListener(new ItemClickEvent.ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String hostname = (String) table.getItem(event.getItemId())
                            .getItemProperty("Host").getValue();
                    Agent agent = HiveUI.getAgentManager().getAgentByHostname(hostname);
                    if (agent != null) {
                        Window terminal = MgmtApplication.createTerminalWindow(Util.wrapAgentToSet(agent));
                        MgmtApplication.addCustomWindow(terminal);
                    } else {
                        show("Agent is not connected");
                    }
                }
            }
        });
        return table;
    }

    private void destroyClusterHandler() {

        UUID trackId = HiveUI.getManager().uninstallCluster(config.getClusterName());

        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackId,
                new Window.CloseListener() {

                    public void windowClose(Window.CloseEvent e) {
                        refreshClustersInfo();
                    }
                }
        );
    }
}
