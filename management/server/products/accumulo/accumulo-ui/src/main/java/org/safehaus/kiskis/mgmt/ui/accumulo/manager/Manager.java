/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.accumulo.manager;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.api.accumulo.Config;
import org.safehaus.kiskis.mgmt.server.ui.ConfirmationDialogCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CompleteEvent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.ui.accumulo.AccumuloUI;
import org.safehaus.kiskis.mgmt.ui.accumulo.common.UiUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * TODO add zk refresh option
 *
 * @author dilshat
 */
public class Manager {

    private final VerticalLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table mastersTable;
    private final Table tracersTable;
    private final Table slavesTable;
    private Config config;

    public Manager() {

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing(true);
        contentRoot.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        contentRoot.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        VerticalLayout content = new VerticalLayout();
        content.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        contentRoot.addComponent(content);
        contentRoot.setComponentAlignment(content, Alignment.TOP_CENTER);
        contentRoot.setMargin(true);

        //tables go here
        mastersTable = UiUtil.createTableTemplate("Masters", 200, contentRoot.getWindow());
        tracersTable = UiUtil.createTableTemplate("Tracers", 200, contentRoot.getWindow());
        slavesTable = UiUtil.createTableTemplate("Slaves", 200, contentRoot.getWindow());
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
                checkNodesStatus(mastersTable);
                checkNodesStatus(slavesTable);
                checkNodesStatus(tracersTable);
            }

        });
        controlsContent.addComponent(checkAllBtn);

        Button startAllBtn = new Button("Start all");
        startAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                startAllNodes(mastersTable);
                startAllNodes(slavesTable);
                startAllNodes(tracersTable);
            }

        });
        controlsContent.addComponent(startAllBtn);

        Button stopAllBtn = new Button("Stop all");
        stopAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                stopAllNodes(mastersTable);
                stopAllNodes(slavesTable);
                stopAllNodes(tracersTable);
            }

        });
        controlsContent.addComponent(stopAllBtn);

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
                                        UUID trackID = AccumuloUI.getAccumuloManager().uninstallCluster(config.getClusterName());
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
                    UiUtil.showMsg("Please, select cluster", contentRoot.getWindow());
                }
            }

        });

        controlsContent.addComponent(destroyClusterBtn);

        //use add node window
//        Button addTracerBtn = new Button("Add Tracer");
//
//        addTracerBtn.addListener(new Button.ClickListener() {
//
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
//                if (config != null) {
//                    MgmtApplication.showConfirmationDialog(
//                            "Confirm adding tracer",
//                            String.format("Do you want to add tracer to the %s cluster?", config.getClusterName()),
//                            "Yes", "No", new ConfirmationDialogCallback() {
//
//                                @Override
//                                public void response(boolean ok) {
//                                    if (ok) {
//                                        UUID trackID = AccumuloUI.getAccumuloManager().addNode(config.getClusterName(), NodeType.TRACER);
//                                        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {
//
//                                            public void windowClose(Window.CloseEvent e) {
//                                                refreshClustersInfo();
//                                            }
//                                        });
//                                    }
//                                }
//                            }
//                    );
//                } else {
//                    UiUtil.showMsg("Please, select cluster", contentRoot.getWindow());
//                }
//            }
//        });
//
//        controlsContent.addComponent(addTracerBtn);
//
//        Button addSlaveBtn = new Button("Add Slave");
//
//        addSlaveBtn.addListener(new Button.ClickListener() {
//
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
//                if (config != null) {
//                    MgmtApplication.showConfirmationDialog(
//                            "Confirm adding slave",
//                            String.format("Do you want to add slave to the %s cluster?", config.getClusterName()),
//                            "Yes", "No", new ConfirmationDialogCallback() {
//
//                                @Override
//                                public void response(boolean ok) {
//                                    if (ok) {
//                                        UUID trackID = AccumuloUI.getAccumuloManager().addNode(config.getClusterName(), NodeType.SLAVE);
//                                        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {
//
//                                            public void windowClose(Window.CloseEvent e) {
//                                                refreshClustersInfo();
//                                            }
//                                        });
//                                    }
//                                }
//                            }
//                    );
//                } else {
//                    UiUtil.showMsg("Please, select cluster", contentRoot.getWindow());
//                }
//            }
//        });
//
//        controlsContent.addComponent(addSlaveBtn);

        content.addComponent(controlsContent);

        content.addComponent(mastersTable);
        content.addComponent(tracersTable);
        content.addComponent(slavesTable);

    }

    public static void checkNodesStatus(Table table) {
        UiUtil.clickAllButtonsInTable(table, "Check");
    }

    public static void startAllNodes(Table table) {
        UiUtil.clickAllButtonsInTable(table, "Start");

    }

    public static void stopAllNodes(Table table) {
        UiUtil.clickAllButtonsInTable(table, "Stop");

    }


    private void populateTable(final Table table, Set<Agent> agents, final boolean masters) {

        table.removeAllItems();

        for (final Agent agent : agents) {
            final Button checkBtn = new Button("Check");
            final Button startBtn = new Button("Start");
            final Button stopBtn = new Button("Stop");
            final Button destroyBtn = new Button("Destroy");
            final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
            stopBtn.setEnabled(false);
            startBtn.setEnabled(false);
            destroyBtn.setEnabled(!masters);
            progressIcon.setVisible(false);

            table.addItem(new Object[]{
                            agent.getHostname(),
                            checkBtn,
                            startBtn,
                            stopBtn,
                            destroyBtn,
                            progressIcon},
                    null
            );

            checkBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    progressIcon.setVisible(true);
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    AccumuloUI.getExecutor().execute(new CheckTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

                        public void onComplete(NodeState state) {
                            synchronized (progressIcon) {
                                if (state == NodeState.RUNNING) {
                                    stopBtn.setEnabled(true);
                                } else if (state == NodeState.STOPPED) {
                                    startBtn.setEnabled(true);
                                }
                                destroyBtn.setEnabled(!masters);
                                progressIcon.setVisible(false);
                            }
                        }
                    }));
                }
            });

            startBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    progressIcon.setVisible(true);
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    AccumuloUI.getExecutor().execute(new StartTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

                        public void onComplete(NodeState state) {
                            synchronized (progressIcon) {
                                if (state == NodeState.RUNNING) {
                                    stopBtn.setEnabled(true);
                                } else {
                                    startBtn.setEnabled(true);
                                }
                                destroyBtn.setEnabled(!masters);
                                progressIcon.setVisible(false);
                            }
                        }
                    }));

                }
            });

            stopBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    progressIcon.setVisible(true);
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    AccumuloUI.getExecutor().execute(new StopTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

                        public void onComplete(NodeState state) {
                            synchronized (progressIcon) {
                                if (state == NodeState.STOPPED) {
                                    startBtn.setEnabled(true);
                                } else {
                                    stopBtn.setEnabled(true);
                                }
                                destroyBtn.setEnabled(!masters);
                                progressIcon.setVisible(false);
                            }
                        }
                    }));
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
                                        UUID trackID = AccumuloUI.getAccumuloManager().destroyNode(config.getClusterName(), agent.getHostname());
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
            populateTable(slavesTable, config.getSlaves(), false);
            populateTable(tracersTable, config.getTracers(), false);
            Set<Agent> masters = new HashSet<Agent>();
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
                clusterCombo.setItemCaption(mongoClusterInfo,
                        mongoClusterInfo.getClusterName());
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

    public Component getContent() {
        return contentRoot;
    }


}
