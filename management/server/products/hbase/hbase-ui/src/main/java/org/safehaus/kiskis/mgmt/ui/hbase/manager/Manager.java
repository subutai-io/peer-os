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
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.hbase.HBaseUI;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        masterTable = createTableTemplate("Master", 100);
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

        Button startClustersBtn = new Button("Start cluster");
        startClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config != null) {
                    UUID trackID = HBaseUI.getHbaseManager().startCluster(config.getClusterName());
                    MgmtApplication.showProgressWindow(HBaseConfig.PRODUCT_KEY, trackID, new Window.CloseListener() {

                        public void windowClose(Window.CloseEvent e) {
                            refreshClustersInfo();
                        }
                    });
                } else {
                    show("Please, select cluster");
                }
            }

        });

        controlsContent.addComponent(startClustersBtn);

        Button stopClustersBtn = new Button("Stop cluster");
        stopClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config != null) {
                    UUID trackID = HBaseUI.getHbaseManager().stopCluster(config.getClusterName());
                    MgmtApplication.showProgressWindow(HBaseConfig.PRODUCT_KEY, trackID, new Window.CloseListener() {

                        public void windowClose(Window.CloseEvent e) {
                            refreshClustersInfo();
                        }
                    });
                } else {
                    show("Please, select cluster");
                }
            }

        });

        controlsContent.addComponent(stopClustersBtn);

        Button checkClustersBtn = new Button("Check cluster");
        checkClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config != null) {
                    UUID trackID = HBaseUI.getHbaseManager().checkCluster(config.getClusterName());
                    MgmtApplication.showProgressWindow(HBaseConfig.PRODUCT_KEY, trackID, new Window.CloseListener() {

                        public void windowClose(Window.CloseEvent e) {
                            refreshClustersInfo();
                        }
                    });
                } else {
                    show("Please, select cluster");
                }
            }

        });

        controlsContent.addComponent(checkClustersBtn);

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
                                        UUID trackID = HBaseUI.getHbaseManager().uninstallCluster(config.getClusterName());
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

    private void populateMasterTable(final Table table, Set<UUID> agents, final HBaseType type) {

        table.removeAllItems();

        for (final UUID uuid : agents) {
            final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
            progressIcon.setVisible(false);

            final Object rowId = table.addItem(new Object[]{
                            uuid,
                            type,
                            progressIcon},
                    null
            );
        }
    }

    private void populateTable(final Table table, Set<UUID> agents, final HBaseType type) {

        table.removeAllItems();

        for (final UUID uuid : agents) {
            final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
            progressIcon.setVisible(false);

            final Object rowId = table.addItem(new Object[]{
                            uuid,
                            type,
                            progressIcon},
                    null
            );
        }
    }

    private void refreshUI() {
        if (config != null) {
            populateTable(quorumTable, config.getQuorum(), HBaseType.HQuorumPeer);
            populateTable(regionTable, config.getRegion(), HBaseType.HRegionServer);

            Set<UUID> masterSet = new HashSet<UUID>();
            masterSet.add(config.getMaster());
            populateMasterTable(masterTable, masterSet, HBaseType.HMaster);

            Set<UUID> bmasterSet = new HashSet<UUID>();
            bmasterSet.add(config.getBackupMasters());
            populateTable(bmasterTable, bmasterSet, HBaseType.BackupMaster);

        } else {
            regionTable.removeAllItems();
            quorumTable.removeAllItems();
            bmasterTable.removeAllItems();
            masterTable.removeAllItems();
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
                for (HBaseConfig config : clusters) {
                    if (config.getClusterName().equals(clusterInfo)) {
                        clusterCombo.setValue(config);
                        return;
                    }
                }
            } else {
                clusterCombo.setValue(clusters.iterator().next());
            }
        }
    }

    private Table createTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Type", HBaseType.class, null);
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
