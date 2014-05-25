package org.safehaus.subutai.ui.flume.manager;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.flume.Config;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.server.ui.ConfirmationDialogCallback;
import org.safehaus.subutai.server.ui.MgmtApplication;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.ui.flume.FlumeUI;

import java.util.*;

public class Manager {

    private final VerticalLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
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
        nodesTable = createTableTemplate("Nodes", 200);
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

        controlsContent.addComponent(clusterCombo);

        Button refreshClustersBtn = new Button("Refresh clusters");
        refreshClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                refreshClustersInfo();
            }
        });

        controlsContent.addComponent(refreshClustersBtn);

        Button destroyClusterBtn = new Button("Destroy cluster");
        destroyClusterBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config != null) {
                    MgmtApplication.showConfirmationDialog(
                            "Cluster destruction confirmation",
                            String.format("Do you want to destroy the %s cluster?",
                                    config.getClusterName()),
                            "Yes", "No", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (ok) {
                                        UUID trackID = FlumeUI.getManager().uninstallCluster(config.getClusterName());
                                        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                                            public void windowClose(Window.CloseEvent e) {
                                                refreshClustersInfo();
                                            }
                                        });
                                    }
                                }
                            }
                    );
                } else {
                    show("Please, select cluster");
                }
            }

        });

        controlsContent.addComponent(destroyClusterBtn);

        Button addNodeBtn = new Button("Add Node");
        addNodeBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config != null) {
                    org.safehaus.subutai.api.hadoop.Config info = FlumeUI.getHadoopManager().getCluster(config.getClusterName());
                    if (info != null) {
                        Set<Agent> nodes = new HashSet<Agent>(info.getAllNodes());
                        nodes.removeAll(config.getNodes());
                        if (!nodes.isEmpty()) {
                            AddNodeWindow addNodeWindow = new AddNodeWindow(config, nodes);
                            MgmtApplication.addCustomWindow(addNodeWindow);
                            addNodeWindow.addListener(new Window.CloseListener() {

                                public void windowClose(Window.CloseEvent e) {
                                    refreshClustersInfo();
                                }
                            });
                        } else {
                            show("All nodes in corresponding Hadoop cluster have Flume installed");
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

        content.addComponent(controlsContent);
        content.addComponent(nodesTable);

    }

    public Component getContent() {
        return contentRoot;
    }

    private void show(String notification) {
        contentRoot.getWindow().showNotification(notification);
    }

    private void populateTable(final Table table, Set<Agent> agents) {

        table.removeAllItems();

        for (final Agent agent : agents) {
            final Button destroyBtn = new Button("Destroy");
            final Button startBtn = new Button("Start");
            final Button stopBtn = new Button("Stop");
            stopBtn.setEnabled(true);
            startBtn.setEnabled(true);

            table.addItem(new Object[]{agent.getHostname(),
                    startBtn, stopBtn, destroyBtn}, null);

            startBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    final UUID trackId = FlumeUI.getManager().startNode(
                            config.getClusterName(), agent.getHostname());

                    MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackId,
                            new Window.CloseListener() {

                                public void windowClose(Window.CloseEvent e) {
                                    ProductOperationView po = FlumeUI.getTracker()
                                            .getProductOperation(Config.PRODUCT_KEY, trackId);
                                    if (po.getState() == ProductOperationState.SUCCEEDED) {
                                        stopBtn.setEnabled(true);
                                    } else {
                                        startBtn.setEnabled(true);
                                    }
                                    destroyBtn.setEnabled(true);
                                }
                            }
                    );
                }
            });

            stopBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    final UUID trackId = FlumeUI.getManager().stopNode(
                            config.getClusterName(), agent.getHostname());

                    MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackId,
                            new Window.CloseListener() {

                                public void windowClose(Window.CloseEvent e) {
                                    ProductOperationView po = FlumeUI.getTracker()
                                            .getProductOperation(Config.PRODUCT_KEY, trackId);
                                    if (po.getState() == ProductOperationState.SUCCEEDED) {
                                        startBtn.setEnabled(true);
                                    } else {
                                        stopBtn.setEnabled(true);
                                    }
                                    destroyBtn.setEnabled(true);
                                }
                            }
                    );
                }
            });

            destroyBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    MgmtApplication.showConfirmationDialog(
                            "Node destruction confirmation",
                            String.format("Do you want to destroy the %s node?", agent.getHostname()),
                            "Yes", "No", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (ok) {
                                        UUID trackID = FlumeUI.getManager().destroyNode(config.getClusterName(), agent.getHostname());
                                        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                                            public void windowClose(Window.CloseEvent e) {
                                                refreshClustersInfo();
                                            }
                                        });
                                    }
                                }
                            }
                    );

                }
            });
        }
    }

    private void refreshUI() {
        if (config != null) {
            populateTable(nodesTable, config.getNodes());
        } else {
            nodesTable.removeAllItems();
        }
    }

    public void refreshClustersInfo() {
        List<Config> clustersInfo = FlumeUI.getManager().getClusters();
        Config clusterInfo = (Config) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if (clustersInfo != null && clustersInfo.size() > 0) {
            for (Config ci : clustersInfo) {
                clusterCombo.addItem(ci);
                clusterCombo.setItemCaption(ci, ci.getClusterName());
            }
            if (clusterInfo != null) {
                for (Config ci : clustersInfo) {
                    if (ci.getClusterName().equals(clusterInfo.getClusterName())) {
                        clusterCombo.setValue(ci);
                        return;
                    }
                }
            } else {
                clusterCombo.setValue(clustersInfo.iterator().next());
            }
        }
    }

    private Table createTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Start", Button.class, null);
        table.addContainerProperty("Stop", Button.class, null);
        table.addContainerProperty("Destroy", Button.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);

        table.addListener(new ItemClickEvent.ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    Agent lxcAgent = FlumeUI.getAgentManager().getAgentByHostname(lxcHostname);
                    if (lxcAgent != null) {
                        Window terminal = MgmtApplication.createTerminalWindow(Util.wrapAgentToSet(lxcAgent));
                        MgmtApplication.addCustomWindow(terminal);
                    } else {
                        show("Agent is not connected");
                    }
                }
            }
        });
        return table;
    }

}
