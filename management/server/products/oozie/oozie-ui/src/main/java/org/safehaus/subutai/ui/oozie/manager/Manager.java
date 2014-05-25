/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.oozie.manager;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.oozie.Config;
import org.safehaus.subutai.server.ui.ConfirmationDialogCallback;
import org.safehaus.subutai.server.ui.MgmtApplication;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.ui.oozie.OozieUI;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author dilshat
 */
public class Manager {

    private final VerticalLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table serverTable;
    private final Table clientsTable;
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
        serverTable = createServerTableTemplate("Server", 200);
        clientsTable = createClientsTableTemplate("Clients", 200);
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

        Button checkAllBtn = new Button("Check all");
        checkAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                checkNodesStatus(serverTable);
            }

        });

        // TODO add restart hadoop button

        Button destroyClusterBtn = new Button("Destroy cluster");
        destroyClusterBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config != null) {
                    MgmtApplication.showConfirmationDialog(
                            "Cluster destruction confirmation",
                            String.format("Do you want to destroy the %s cluster?", config.getClusterName()),
                            "Yes", "No", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (ok) {
                                        UUID trackID = OozieUI.getOozieManager().uninstallCluster(config.getClusterName());
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
        content.addComponent(controlsContent);
        content.addComponent(serverTable);
        content.addComponent(clientsTable);

    }

    public static void checkNodesStatus(Table table) {
        for (Object o : table.getItemIds()) {
            int rowId = (Integer) o;
            Item row = table.getItem(rowId);
            Button checkBtn = (Button) (row.getItemProperty("Check").getValue());
            checkBtn.click();
        }
    }

    public Component getContent() {
        return contentRoot;
    }

    private void show(String notification) {
        contentRoot.getWindow().showNotification(notification);
    }

    private void populateServerTable(final Table table, final Agent agent) {

        table.removeAllItems();
        final Button checkBtn = new Button("Check");
        final Button startBtn = new Button("Start");
        final Button stopBtn = new Button("Stop");
        final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
//        stopBtn.setEnabled(false);
//        startBtn.setEnabled(false);
        progressIcon.setVisible(false);

        final Object rowId = table.addItem(new Object[]{
                        agent.getHostname(),
//                        checkBtn,
//                        startBtn,
//                        stopBtn,
                        progressIcon},
                null
        );

        checkBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                progressIcon.setVisible(true);
//                startBtn.setEnabled(false);
//                stopBtn.setEnabled(false);

//                OozieUI.getExecutor().execute(new CheckTask(agent, new CompleteEvent() {
//
//                    public void onComplete(NodeState state) {
//                        synchronized (progressIcon) {
//                            if (state == NodeState.RUNNING) {
//                                stopBtn.setEnabled(true);
//                            } else if (state == NodeState.STOPPED) {
//                                startBtn.setEnabled(true);
//                            }
//                            progressIcon.setVisible(false);
//                        }
//                    }
//                }));

                UUID trackID = OozieUI.getOozieManager().checkServerStatus(config);
                MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                    public void windowClose(Window.CloseEvent e) {
                        refreshClustersInfo();
                    }
                });
            }
        });

        startBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                progressIcon.setVisible(true);
                startBtn.setEnabled(false);
                stopBtn.setEnabled(false);

//                OozieUI.getExecutor().execute(new StartTask(agent, new CompleteEvent() {
//
//                    public void onComplete(NodeState state) {
//                        synchronized (progressIcon) {
//                            if (state == NodeState.RUNNING) {
//                                stopBtn.setEnabled(true);
//                            } else {
//                                startBtn.setEnabled(true);
//                            }
//                            progressIcon.setVisible(false);
//                        }
//                    }
//                }));
                UUID trackID = OozieUI.getOozieManager().startServer(config);
                MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                    public void windowClose(Window.CloseEvent e) {
                        refreshClustersInfo();
                    }
                });

            }
        });

        stopBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                progressIcon.setVisible(true);
//                startBtn.setEnabled(false);
//                stopBtn.setEnabled(false);

//                OozieUI.getExecutor().execute(new StopTask(agent, new CompleteEvent() {
//
//                    public void onComplete(NodeState state) {
//                        synchronized (progressIcon) {
//                            if (state == NodeState.STOPPED) {
//                                startBtn.setEnabled(true);
//                            } else {
//                                stopBtn.setEnabled(true);
//                            }
//                            progressIcon.setVisible(false);
//                        }
//                    }
//                }));

                UUID trackID = OozieUI.getOozieManager().stopServer(config);
                MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                    public void windowClose(Window.CloseEvent e) {
                        refreshClustersInfo();
                    }
                });
            }
        });

    }

    private void populateClientsTable(final Table table, Set<Agent> agents) {

        table.removeAllItems();

        for (final Agent agent : agents) {
            final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
            progressIcon.setVisible(false);

            final Object rowId = table.addItem(new Object[]{
                            agent.getHostname(),
                    },
                    null
            );
        }
    }

    private void refreshUI() {
        if (config != null) {
            populateServerTable(serverTable, config.getServer());
            populateClientsTable(clientsTable, config.getClients());
        } else {
            serverTable.removeAllItems();
            clientsTable.removeAllItems();
        }
    }

    public void refreshClustersInfo() {
        List<Config> info = OozieUI.getOozieManager().getClusters();
        Config clusterInfo = (Config) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if (info != null && info.size() > 0) {
            for (Config oozieConfig : info) {
                clusterCombo.addItem(oozieConfig);
                clusterCombo.setItemCaption(oozieConfig,
                        oozieConfig.getClusterName());
            }
            if (clusterInfo != null) {
                for (Config oozieInfo : info) {
                    if (oozieInfo.getClusterName().equals(clusterInfo.getClusterName())) {
                        clusterCombo.setValue(oozieInfo);
                        return;
                    }
                }
            } else {
                clusterCombo.setValue(info.iterator().next());
            }
        }
    }

    private Table createServerTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Check", Button.class, null);
        table.addContainerProperty("Start", Button.class, null);
        table.addContainerProperty("Stop", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);

        table.addListener(new ItemClickEvent.ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    Agent lxcAgent = OozieUI.getAgentManager().getAgentByHostname(lxcHostname);
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

    private Table createClientsTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);

        table.addListener(new ItemClickEvent.ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    Agent lxcAgent = OozieUI.getAgentManager().getAgentByHostname(lxcHostname);
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
