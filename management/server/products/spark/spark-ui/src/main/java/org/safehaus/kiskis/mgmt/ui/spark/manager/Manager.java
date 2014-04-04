/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.spark.manager;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.spark.Config;
import org.safehaus.kiskis.mgmt.server.ui.ConfirmationDialogCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.spark.SparkUI;

/**
 *
 * @author dilshat
 * @todo add hadoop node selection for addNode
 */
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
                            String.format("Do you want to destroy the %s cluster?", config.getClusterName()),
                            "Yes", "No", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (ok) {
                                        UUID trackID = SparkUI.getSparkManager().uninstallCluster(config.getClusterName());
                                        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                                            public void windowClose(Window.CloseEvent e) {
                                                refreshClustersInfo();
                                            }
                                        });
                                    }
                                }
                            });
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
                    HadoopClusterInfo info = SparkUI.getDbManager().
                            getInfo(HadoopClusterInfo.SOURCE,
                                    config.getClusterName(),
                                    HadoopClusterInfo.class);
                    if (info != null) {
                        Set<Agent> nodes = new HashSet<Agent>(info.getAllAgents());
                        nodes.removeAll(config.getSlaveNodes());
                        if (!nodes.isEmpty()) {
                            AddNodeWindow addNodeWindow = new AddNodeWindow(config, nodes);
                            MgmtApplication.addCustomWindow(addNodeWindow);
                            addNodeWindow.addListener(new Window.CloseListener() {

                                public void windowClose(Window.CloseEvent e) {
                                    refreshClustersInfo();
                                }
                            });
                        } else {
                            show("All nodes in corresponding Hadoop cluster have Spark installed");
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

        for (Iterator it = agents.iterator(); it.hasNext();) {
            final Agent agent = (Agent) it.next();

            final Button destroyBtn = new Button("Destroy");

            final Object rowId = table.addItem(new Object[]{
                agent.getHostname(),
                destroyBtn
            },
                    null);

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
                                        UUID trackID = SparkUI.getSparkManager().destroySlaveNode(config.getClusterName(), agent.getHostname());
                                        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, new Window.CloseListener() {

                                            public void windowClose(Window.CloseEvent e) {
                                                refreshClustersInfo();
                                            }
                                        });
                                    }
                                }
                            });

                }
            });
        }
    }

    private void refreshUI() {
        if (config != null) {
            populateTable(nodesTable, config.getSlaveNodes());
        } else {
            nodesTable.removeAllItems();
        }
    }

    public void refreshClustersInfo() {
        List<Config> clustersInfo = SparkUI.getSparkManager().getClusters();
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

    private Table createTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
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
                    Agent lxcAgent = SparkUI.getAgentManager().getAgentByHostname(lxcHostname);
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
