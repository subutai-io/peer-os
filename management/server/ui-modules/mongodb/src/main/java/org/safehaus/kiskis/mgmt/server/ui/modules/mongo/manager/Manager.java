/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.NodeType;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.callback.DestroyCfgSrvCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.callback.CheckStatusCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.callback.DestroyDataNodeCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.callback.StopNodeCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.callback.DestroyRouterCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.callback.StartNodeCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.operation.DestroyNodeOperation;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.ConfirmationDialogCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Constants;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.MongoDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Tasks;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.entity.MongoClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AsyncTaskRunner;

/**
 *
 * @author dilshat
 *
 */
public class Manager {

    private static final Logger LOG = Logger.getLogger(Manager.class.getName());

    private final VerticalLayout contentRoot;
    private final AgentManager agentManager;
    private final ComboBox clusterCombo;
    private final Table configServersTable;
    private final Table routersTable;
    private final Table dataNodesTable;
    private final AsyncTaskRunner taskRunner;
    private DestroyClusterWindow destroyWindow;
    private AddNodeWindow addNodeWindow;
    private ClusterConfig config;

    public Manager(final AsyncTaskRunner taskRunner) {
        this.taskRunner = taskRunner;
        agentManager = ServiceLocator.getService(AgentManager.class);

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
        configServersTable = createTableTemplate("Config Servers", 150);
        routersTable = createTableTemplate("Query Routers", 150);
        dataNodesTable = createTableTemplate("Data Nodes", 270);
        //tables go here

        Label clusterNameLabel = new Label("Select the cluster");
        content.addComponent(clusterNameLabel);

        HorizontalLayout topContent = new HorizontalLayout();
        topContent.setSpacing(true);

        clusterCombo = new ComboBox();
        clusterCombo.setMultiSelect(false);
        clusterCombo.setImmediate(true);
        clusterCombo.setTextInputAllowed(false);
        clusterCombo.setWidth(300, Sizeable.UNITS_PIXELS);
        clusterCombo.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                config = constructClusterConfig((MongoClusterInfo) event.getProperty().getValue());
                refreshUI();
            }
        });

        topContent.addComponent(clusterCombo);

        Button refreshClustersBtn = new Button("Refresh clusters");
        refreshClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                refreshClustersInfo();
            }
        });

        topContent.addComponent(refreshClustersBtn);

        Button checkAllBtn = new Button("Check all");
        checkAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                checkNodesStatus(configServersTable);
                checkNodesStatus(routersTable);
                checkNodesStatus(dataNodesTable);
            }

        });

        topContent.addComponent(checkAllBtn);

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
                                        destroyWindow = new DestroyClusterWindow(config, taskRunner);
                                        MgmtApplication.addCustomWindow(destroyWindow);
                                        destroyWindow.addListener(new Window.CloseListener() {

                                            @Override
                                            public void windowClose(Window.CloseEvent e) {
                                                if (destroyWindow.isSucceeded()) {
                                                    refreshClustersInfo();
                                                }
//                                                taskRunner.removeAllTaskCallbacks();
                                            }
                                        });
                                        destroyWindow.startOperation();
                                    }
                                }
                            });
                } else {
                    show("Please, select cluster");
                }
            }

        });

        topContent.addComponent(destroyClusterBtn);

        Button addNodeBtn = new Button("Add New Node");

        addNodeBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (config != null) {
                    addNodeWindow = new AddNodeWindow(
                            config, (MongoClusterInfo) clusterCombo.getValue(), taskRunner);
                    MgmtApplication.addCustomWindow(addNodeWindow);
                    addNodeWindow.addListener(new Window.CloseListener() {

                        @Override
                        public void windowClose(Window.CloseEvent e) {
                            //refresh clusters and show the current one again
                            if (addNodeWindow.isSucceeded()) {
                                refreshClustersInfo();
                            }
//                            taskRunner.removeAllTaskCallbacks();
                        }
                    });
                } else {
                    show("Please, select cluster");
                }
            }
        });

        topContent.addComponent(addNodeBtn);

        content.addComponent(topContent);

        HorizontalLayout midContent = new HorizontalLayout();
        midContent.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        midContent.addComponent(configServersTable);

        midContent.addComponent(routersTable);

        content.addComponent(midContent);

        content.addComponent(dataNodesTable);

        refreshClustersInfo();
    }

    public Component getContent() {
        return contentRoot;
    }

    private void show(String notification) {
        contentRoot.getWindow().showNotification(notification);
    }

    private void populateTable(final Table table, Set<Agent> agents, final NodeType nodeType) {

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

            final Object rowId = table.addItem(new Object[]{
                agent.getHostname(),
                checkBtn,
                startBtn,
                stopBtn,
                destroyBtn,
                progressIcon},
                    null);

            checkBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Task checkStatusTask = Tasks.getCheckStatusTask(
                            new HashSet<Agent>(Arrays.asList(agent)),
                            nodeType);
                    taskRunner.executeTask(checkStatusTask, new CheckStatusCallback(taskRunner, progressIcon, startBtn, stopBtn, destroyBtn));
                }
            });

            startBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Task startNodeTask = null;
                    if (nodeType == NodeType.CONFIG_NODE) {
                        startNodeTask = Tasks.getStartConfigServersTask(
                                Util.wrapAgentToSet(agent));

                    } else if (nodeType == NodeType.DATA_NODE) {

                        startNodeTask = Tasks.getStartReplicaSetTask(
                                Util.wrapAgentToSet(agent));

                    } else if (nodeType == NodeType.ROUTER_NODE) {
                        startNodeTask = Tasks.getStartRoutersTask(
                                Util.wrapAgentToSet(agent),
                                config.getConfigServers());

                    }
                    if (startNodeTask != null) {
                        taskRunner.executeTask(startNodeTask,
                                new StartNodeCallback(taskRunner, progressIcon, checkBtn, startBtn, stopBtn, destroyBtn));
                    }
                }
            });

            stopBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Task stopNodeTask = Tasks.getStopMongoTask(
                            Util.wrapAgentToSet(agent));

                    taskRunner.executeTask(stopNodeTask,
                            new StopNodeCallback(progressIcon, checkBtn, startBtn, stopBtn, destroyBtn));
                }
            });

            destroyBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if (nodeType == NodeType.CONFIG_NODE) {
                        Operation destroyCfgSrvOperation = new DestroyNodeOperation(agent, config, nodeType);
                        taskRunner.executeTask(destroyCfgSrvOperation.getNextTask(),
                                new DestroyCfgSrvCallback(contentRoot.getWindow(),
                                        (MongoClusterInfo) clusterCombo.getValue(),
                                        config, agent,
                                        configServersTable, routersTable,
                                        rowId, destroyCfgSrvOperation,
                                        taskRunner, progressIcon,
                                        checkBtn, startBtn,
                                        stopBtn, destroyBtn));

                    } else if (nodeType == NodeType.DATA_NODE) {
                        Operation destroyDataNodeOperation = new DestroyNodeOperation(agent, config, nodeType);
                        taskRunner.executeTask(destroyDataNodeOperation.getNextTask(),
                                new DestroyDataNodeCallback(
                                        contentRoot.getWindow(), agentManager,
                                        (MongoClusterInfo) clusterCombo.getValue(),
                                        config, agent,
                                        dataNodesTable, rowId,
                                        destroyDataNodeOperation,
                                        progressIcon,
                                        checkBtn, startBtn,
                                        stopBtn, destroyBtn));

                    } else if (nodeType == NodeType.ROUTER_NODE) {
                        Operation destroyRouterOperation = new DestroyNodeOperation(agent, config, nodeType);
                        taskRunner.executeTask(destroyRouterOperation.getNextTask(),
                                new DestroyRouterCallback(contentRoot.getWindow(),
                                        (MongoClusterInfo) clusterCombo.getValue(),
                                        config, agent,
                                        routersTable,
                                        rowId, destroyRouterOperation,
                                        progressIcon,
                                        checkBtn, startBtn,
                                        stopBtn, destroyBtn));
                    }
                }
            });
        }
    }

    private ClusterConfig constructClusterConfig(MongoClusterInfo clusterInfo) {
        ClusterConfig cfg = null;

        if (clusterInfo != null) {
            cfg = new ClusterConfig();
            cfg.setConfigServers(new HashSet<Agent>());
            cfg.setRouterServers(new HashSet<Agent>());
            cfg.setDataNodes(new HashSet<Agent>());
            cfg.setClusterName(clusterInfo.getClusterName());
            cfg.setReplicaSetName(clusterInfo.getReplicaSetName());

            for (UUID agentUUID : clusterInfo.getConfigServers()) {
                Agent agent = agentManager.getAgentByUUID(agentUUID);
                if (agent != null) {
                    cfg.getConfigServers().add(agent);
                }
            }
            for (UUID agentUUID : clusterInfo.getRouters()) {
                Agent agent = agentManager.getAgentByUUID(agentUUID);
                if (agent != null) {
                    cfg.getRouterServers().add(agent);
                }
            }
            for (UUID agentUUID : clusterInfo.getDataNodes()) {
                Agent agent = agentManager.getAgentByUUID(agentUUID);
                if (agent != null) {
                    cfg.getDataNodes().add(agent);
                }
            }
        }
        return cfg;
    }

    private void refreshUI() {
        if (config != null) {
            populateTable(configServersTable, config.getConfigServers(), NodeType.CONFIG_NODE);
            populateTable(routersTable, config.getRouterServers(), NodeType.ROUTER_NODE);
            populateTable(dataNodesTable, config.getDataNodes(), NodeType.DATA_NODE);
        } else {
            configServersTable.removeAllItems();
            routersTable.removeAllItems();
            dataNodesTable.removeAllItems();
        }
    }

    private void refreshClustersInfo() {
        List<MongoClusterInfo> mongoClusterInfos = MongoDAO.getMongoClustersInfo();
        MongoClusterInfo clusterInfo = (MongoClusterInfo) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if (mongoClusterInfos != null && mongoClusterInfos.size() > 0) {
            for (MongoClusterInfo mongoClusterInfo : mongoClusterInfos) {
                clusterCombo.addItem(mongoClusterInfo);
                clusterCombo.setItemCaption(mongoClusterInfo,
                        String.format("Name: %s RS: %s", mongoClusterInfo.getClusterName(), mongoClusterInfo.getReplicaSetName()));
            }
            if (clusterInfo != null) {
                for (MongoClusterInfo mongoClusterInfo : mongoClusterInfos) {
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

    public static void checkNodesStatus(Table table) {
        for (Iterator it = table.getItemIds().iterator(); it.hasNext();) {
            int rowId = (Integer) it.next();
            Item row = table.getItem(rowId);
            Button checkBtn = (Button) (row.getItemProperty(Constants.TABLE_CHECK_PROPERTY).getValue());
            checkBtn.click();
        }
    }

    private Table createTableTemplate(String caption, int size) {
        Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty(Constants.TABLE_CHECK_PROPERTY, Button.class, null);
        table.addContainerProperty("Start", Button.class, null);
        table.addContainerProperty("Stop", Button.class, null);
        table.addContainerProperty("Destroy", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);
        return table;
    }

}
