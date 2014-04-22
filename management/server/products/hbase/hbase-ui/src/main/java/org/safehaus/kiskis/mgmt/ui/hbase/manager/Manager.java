/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.hbase.manager;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.api.hbase.HBaseConfig;
import org.safehaus.kiskis.mgmt.api.hbase.HBaseType;
import org.safehaus.kiskis.mgmt.server.ui.ConfirmationDialogCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CompleteEvent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.ui.hbase.HBaseUI;

import java.util.*;

/**
 * @author dilshat
 */
public class Manager {

    private final VerticalLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table masterTable;
    private final Table regionTable;
    private final Table quorumTable;
    private final Table bmasterTable;
    private HBaseConfig config;

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
        masterTable = createMasterTableTemplate("Master", 100);
        regionTable = createTableTemplate("Region", 100);
        quorumTable = createTableTemplate("Quorum", 100);
        bmasterTable = createTableTemplate("Backup master", 100);
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
                config = (HBaseConfig) event.getProperty().getValue();
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
                            String.format("Do you want to destroy the %s cluster?", config.getClusterName()),
                            "Yes", "No", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (ok) {
                                        UUID trackID = HBaseUI.getHbaseManager().uninstallCluster(config);
                                        MgmtApplication.showProgressWindow(HBaseConfig.PRODUCT_KEY, trackID, new Window.CloseListener() {

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

        content.addComponent(masterTable);
        content.addComponent(regionTable);
        content.addComponent(quorumTable);
        content.addComponent(bmasterTable);

    }

    public Component getContent() {
        return contentRoot;
    }

    private void show(String notification) {
        contentRoot.getWindow().showNotification(notification);
    }

    private void populateMasterTable(final Table table, Set<Agent> agents, final HBaseType type) {

        table.removeAllItems();

        for (Iterator it = agents.iterator(); it.hasNext(); ) {
            final Agent agent = (Agent) it.next();

            final Button checkBtn = new Button("Check");
//            final Button startBtn = new Button("Start");
//            final Button stopBtn = new Button("Stop");
            final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
//            stopBtn.setEnabled(false);
//            startBtn.setEnabled(false);
            progressIcon.setVisible(false);

            final Object rowId = table.addItem(new Object[]{
                            agent.getHostname(),
                            type,
                            checkBtn,
//                            startBtn,
//                            stopBtn,
                            progressIcon},
                    null
            );

            checkBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    progressIcon.setVisible(true);
//                    startBtn.setEnabled(false);
//                    stopBtn.setEnabled(false);

                    HBaseUI.getExecutor().execute(new CheckTask(type, config.getClusterName(), agent.getHostname(), new CompleteEvent() {

                        public void onComplete(NodeState state) {
                            synchronized (progressIcon) {
//                                if (state == NodeState.RUNNING) {
//                                    stopBtn.setEnabled(true);
//                                } else if (state == NodeState.STOPPED) {
//                                    startBtn.setEnabled(true);
//                                }
                                show(state.toString());
                                progressIcon.setVisible(false);
                            }
                        }
                    }));
                }
            });

//            startBtn.addListener(new Button.ClickListener() {
//
//                @Override
//                public void buttonClick(Button.ClickEvent event) {
//
//                    progressIcon.setVisible(true);
////                    startBtn.setEnabled(false);
////                    stopBtn.setEnabled(false);
//
//                    HBaseUI.getExecutor().execute(new StartTask(type, config.getClusterName(), new CompleteEvent() {
//
//                        public void onComplete(NodeState state) {
//                            synchronized (progressIcon) {
////                                if (state == NodeState.RUNNING) {
////                                    stopBtn.setEnabled(true);
////                                } else {
////                                    startBtn.setEnabled(true);
////                                }
//                                progressIcon.setVisible(false);
//                            }
//                        }
//                    }));
//
//                }
//            });
//
//            stopBtn.addListener(new Button.ClickListener() {
//
//                @Override
//                public void buttonClick(Button.ClickEvent event) {
//
//                    progressIcon.setVisible(true);
////                    startBtn.setEnabled(false);
////                    stopBtn.setEnabled(false);
//
//                    HBaseUI.getExecutor().execute(new StopTask(type, config.getClusterName(), new CompleteEvent() {
//
//                        public void onComplete(NodeState state) {
//                            synchronized (progressIcon) {
//                                if (state == NodeState.STOPPED) {
//                                    startBtn.setEnabled(true);
//                                } else {
//                                    stopBtn.setEnabled(true);
//                                }
//                                progressIcon.setVisible(false);
//                            }
//                        }
//                    }));
//                }
//            });

        }
    }
    private void populateTable(final Table table, Set<Agent> agents, final HBaseType type) {

        table.removeAllItems();

        for (Iterator it = agents.iterator(); it.hasNext(); ) {
            final Agent agent = (Agent) it.next();

            final Button checkBtn = new Button("Check");
            final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
            progressIcon.setVisible(false);

            final Object rowId = table.addItem(new Object[]{
                            agent.getHostname(),
                            type,
                            checkBtn,
                            progressIcon},
                    null
            );

            checkBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    progressIcon.setVisible(true);

                    HBaseUI.getExecutor().execute(new CheckTask(type, config.getClusterName(), agent.getHostname(), new CompleteEvent() {

                        public void onComplete(NodeState state) {
                            synchronized (progressIcon) {
                                show(state.toString());
                                progressIcon.setVisible(false);
                            }
                        }
                    }));
                }
            });

        }
    }

    private void refreshUI() {
        if (config != null) {
            populateTable(quorumTable, config.getQuorum(), HBaseType.HQuorumPeer);
            populateTable(regionTable, config.getRegion(), HBaseType.HRegionServer);

            Set<Agent> masterSet = new HashSet<Agent>();
            masterSet.add(config.getMaster());
            populateMasterTable(masterTable, masterSet, HBaseType.HMaster);

            Set<Agent> bmasterSet = new HashSet<Agent>();
            bmasterSet.add(config.getBackupMasters());
            populateTable(bmasterTable, bmasterSet, HBaseType.BackupMaster);

        } else {
            regionTable.removeAllItems();
        }
    }

    public void refreshClustersInfo() {
        List<HBaseConfig> clusters = HBaseUI.getHbaseManager().getClusters();
        HBaseConfig clusterInfo = (HBaseConfig) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if (clusters != null && clusters.size() > 0) {
            for (HBaseConfig info : clusters) {
                clusterCombo.addItem(info);
                clusterCombo.setItemCaption(info,
                        info.getClusterName());
            }
            if (clusterInfo != null) {
                for (HBaseConfig hBaseConfig : clusters) {
                    if (hBaseConfig.getClusterName().equals(clusterInfo)) {
                        clusterCombo.setValue(hBaseConfig);
                        return;
                    }
                }
            } else {
                clusterCombo.setValue(clusters.iterator().next());
            }
        }
    }

    public static void checkNodesStatus(Table table) {
        for (Iterator it = table.getItemIds().iterator(); it.hasNext(); ) {
            int rowId = (Integer) it.next();
            Item row = table.getItem(rowId);
            Button checkBtn = (Button) (row.getItemProperty("Check").getValue());
            checkBtn.click();
        }
    }

    private Table createMasterTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Type", HBaseType.class, null);
        table.addContainerProperty("Check", Button.class, null);
//        table.addContainerProperty("Start", Button.class, null);
//        table.addContainerProperty("Stop", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setSizeFull();
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);

        table.addListener(new ItemClickEvent.ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    Agent lxcAgent = HBaseUI.getAgentManager().getAgentByHostname(lxcHostname);
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
    private Table createTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Type", HBaseType.class, null);
        table.addContainerProperty("Check", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setSizeFull();
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);

        table.addListener(new ItemClickEvent.ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    Agent lxcAgent = HBaseUI.getAgentManager().getAgentByHostname(lxcHostname);
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
