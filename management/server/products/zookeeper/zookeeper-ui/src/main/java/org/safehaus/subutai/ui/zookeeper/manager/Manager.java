/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.zookeeper.manager;

import com.google.common.base.Strings;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.server.ui.ConfirmationDialogCallback;
import org.safehaus.subutai.server.ui.MgmtApplication;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.CompleteEvent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.shared.protocol.enums.NodeState;
import org.safehaus.subutai.ui.zookeeper.ZookeeperUI;

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
    private final Table nodesTable;
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

        Button checkAllBtn = new Button("Check all");
        checkAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                checkNodesStatus(nodesTable);
            }

        });
        controlsContent.addComponent(checkAllBtn);

        Button startAllBtn = new Button("Start all");
        startAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                startAllNodes(nodesTable);
            }

        });
        controlsContent.addComponent(startAllBtn);

        Button stopAllBtn = new Button("Stop all");
        stopAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                stopAllNodes(nodesTable);
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
                                        UUID trackID = ZookeeperUI.getManager().uninstallCluster(config.getClusterName());
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
                    if (config.isStandalone()) {
                        MgmtApplication.showConfirmationDialog(
                                "Confirm adding node",
                                String.format("Do you want to add node to the %s cluster?", config.getClusterName()),
                                "Yes", "No", new ConfirmationDialogCallback() {

                                    @Override
                                    public void response(boolean ok) {
                                        if (ok) {
                                            UUID trackID = ZookeeperUI.getManager().addNode(config.getClusterName());
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
                        org.safehaus.subutai.api.hadoop.Config info = ZookeeperUI.getHadoopManager().getCluster(config.getClusterName());

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
                                show("All nodes in corresponding Hadoop cluster have Zookeeper installed");
                            }
                        } else {
                            show("Hadoop cluster info not found");
                        }
                    }
                } else {
                    show("Please, select cluster");
                }
            }
        });
        controlsContent.addComponent(addNodeBtn);

        HorizontalLayout customPropertyContent = new HorizontalLayout();
        customPropertyContent.setSpacing(true);

        Label fileLabel = new Label("File");
        customPropertyContent.addComponent(fileLabel);
        final TextField fileTextField = new TextField();
        customPropertyContent.addComponent(fileTextField);
        Label propertyNameLabel = new Label("Property Name");
        customPropertyContent.addComponent(propertyNameLabel);
        final TextField propertyNameTextField = new TextField();
        customPropertyContent.addComponent(propertyNameTextField);

        Button removePropertyBtn = new Button("Remove");
        removePropertyBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                if (config != null) {
                    String fileName = (String) fileTextField.getValue();
                    String propertyName = (String) propertyNameTextField.getValue();
                    if (Strings.isNullOrEmpty(fileName)) {
                        show("Please, specify file name where property resides");
                    } else if (Strings.isNullOrEmpty(propertyName)) {
                        show("Please, specify property name to remove");
                    } else {
                        UUID trackID = ZookeeperUI.getManager().removeProperty(config.getClusterName(), fileName, propertyName);
                        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, null);
                    }
                } else {
                    show("Please, select cluster");
                }
            }
        });
        customPropertyContent.addComponent(removePropertyBtn);

        Label propertyValueLabel = new Label("Property Value");
        customPropertyContent.addComponent(propertyValueLabel);
        final TextField propertyValueTextField = new TextField();
        customPropertyContent.addComponent(propertyValueTextField);
        Button addPropertyBtn = new Button("Add");
        addPropertyBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                if (config != null) {
                    String fileName = (String) fileTextField.getValue();
                    String propertyName = (String) propertyNameTextField.getValue();
                    String propertyValue = (String) propertyValueTextField.getValue();
                    if (Strings.isNullOrEmpty(fileName)) {
                        show("Please, specify file name where property will be added");
                    } else if (Strings.isNullOrEmpty(propertyName)) {
                        show("Please, specify property name to add");
                    } else if (Strings.isNullOrEmpty(propertyValue)) {
                        show("Please, specify property value to set");
                    } else {
                        UUID trackID = ZookeeperUI.getManager().addProperty(config.getClusterName(), fileName, propertyName, propertyValue);
                        MgmtApplication.showProgressWindow(Config.PRODUCT_KEY, trackID, null);
                    }
                } else {
                    show("Please, select cluster");
                }
            }
        });
        customPropertyContent.addComponent(addPropertyBtn);

        content.addComponent(controlsContent);
        content.addComponent(customPropertyContent);
        content.addComponent(nodesTable);

        content.setComponentAlignment(controlsContent, Alignment.TOP_RIGHT);
        content.setComponentAlignment(customPropertyContent, Alignment.TOP_RIGHT);
        content.setComponentAlignment(nodesTable, Alignment.TOP_CENTER);

    }

    public static void checkNodesStatus(Table table) {
        for (Object o : table.getItemIds()) {
            int rowId = (Integer) o;
            Item row = table.getItem(rowId);
            Button checkBtn = (Button) (row.getItemProperty("Check").getValue());
            checkBtn.click();
        }
    }

    public static void startAllNodes(Table table) {
        for (Object o : table.getItemIds()) {
            int rowId = (Integer) o;
            Item row = table.getItem(rowId);
            Button checkBtn = (Button) (row.getItemProperty("Start").getValue());
            checkBtn.click();
        }
    }

    public static void stopAllNodes(Table table) {
        for (Object o : table.getItemIds()) {
            int rowId = (Integer) o;
            Item row = table.getItem(rowId);
            Button checkBtn = (Button) (row.getItemProperty("Stop").getValue());
            checkBtn.click();
        }
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
            final Button checkBtn = new Button("Check");
            final Button startBtn = new Button("Start");
            final Button stopBtn = new Button("Stop");
            final Button destroyBtn = new Button("Destroy");
            final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
            stopBtn.setEnabled(false);
            startBtn.setEnabled(false);
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

                    ZookeeperUI.getExecutor().execute(new CheckTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

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

            startBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    progressIcon.setVisible(true);
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    ZookeeperUI.getExecutor().execute(new StartTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

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

            stopBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    progressIcon.setVisible(true);
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(false);
                    destroyBtn.setEnabled(false);

                    ZookeeperUI.getExecutor().execute(new StopTask(config.getClusterName(), agent.getHostname(), new CompleteEvent() {

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
                                        UUID trackID = ZookeeperUI.getManager().destroyNode(config.getClusterName(), agent.getHostname());
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
        List<Config> mongoClusterInfos = ZookeeperUI.getManager().getClusters();
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

    private Table createTableTemplate(String caption, int size) {
        final Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Check", Button.class, null);
        table.addContainerProperty("Start", Button.class, null);
        table.addContainerProperty("Stop", Button.class, null);
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
                    String lxcHostname = (String) table.getItem(event.getItemId()).getItemProperty("Host").getValue();
                    Agent lxcAgent = ZookeeperUI.getAgentManager().getAgentByHostname(lxcHostname);
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
