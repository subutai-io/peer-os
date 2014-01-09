/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.data.Item;
import org.safehaus.kiskis.mgmt.shared.protocol.ExpiringCache;
import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Commands;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Constants;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.MongoClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 *
 */
public class Manager implements ResponseListener {
    /*
     TODO:
     1) find primary node 
     2) destroy node (either unregister from primary after check status command and clean data and ui or do it in destroy task)
     3) add node
     4) overall cluster check status
     5) destroy cluster
    
     */

    private static final Logger LOG = Logger.getLogger(Manager.class.getName());

    private final VerticalLayout contentRoot;
    private final CommandManagerInterface commandManager;
    private final AgentManagerInterface agentManager;
    private final ComboBox clusterCombo;
    private final ExpiringCache<UUID, ManagerAction> actionsCache = new ExpiringCache<UUID, ManagerAction>();
    private MongoClusterInfo clusterInfo;

    public Manager() {
        //get db and transport managers
        agentManager = ServiceLocator.getService(AgentManagerInterface.class);
        commandManager = ServiceLocator.getService(CommandManagerInterface.class);

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing(true);
        contentRoot.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        contentRoot.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        contentRoot.addComponent(content);
        contentRoot.setComponentAlignment(content, Alignment.TOP_CENTER);
        contentRoot.setMargin(true);

        //tables go here
        final Table configServersTable = createTableTemplate("Config Servers");
        final Table routersTable = createTableTemplate("Query Routers");
        final Table dataNodesTable = createTableTemplate("Data Nodes");
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
                if (event.getProperty().getValue() instanceof MongoClusterInfo) {
                    clusterInfo = (MongoClusterInfo) event.getProperty().getValue();
                    populateTable(configServersTable, clusterInfo.getConfigServers(), NodeType.CONFIG_NODE);
                    populateTable(routersTable, clusterInfo.getRouters(), NodeType.ROUTER_NODE);
                    populateTable(dataNodesTable, clusterInfo.getDataNodes(), NodeType.DATA_NODE);
                    actionsCache.clear();
                }
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

    private void executeManagerAction(ManagerActionType managerActionType, Agent agent, NodeType nodeType, Item row) {

        if (managerActionType == ManagerActionType.CHECK_NODE_STATUS) {

            Task checkTask = Util.createTask("Check mongo node status");
            Command checkCommand = Commands.getCheckInstanceRunningCommand(
                    MessageFormat.format("{0}{1}", agent.getHostname(), Constants.DOMAIN),
                    getNodePort(nodeType));
            checkCommand.getRequest().setUuid(agent.getUuid());
            checkCommand.getRequest().setTaskUuid(checkTask.getUuid());
            checkCommand.getRequest().setRequestSequenceNumber(checkTask.getIncrementedReqSeqNumber());
            if (commandManager.executeCommand(checkCommand)) {
                ManagerAction managerAction = new ManagerAction(
                        checkTask, managerActionType,
                        row, agent, nodeType);
                actionsCache.put(checkTask.getUuid(), managerAction,
                        checkCommand.getRequest().getTimeout() * 1000 + 2000);
            }
        } else if (managerActionType == ManagerActionType.STOP_NODE) {

            Task stopTask = Util.createTask("Stop mongo node");
            Command stopCommand = Commands.getStopNodeCommand();
            stopCommand.getRequest().setUuid(agent.getUuid());
            stopCommand.getRequest().setTaskUuid(stopTask.getUuid());
            stopCommand.getRequest().setRequestSequenceNumber(stopTask.getIncrementedReqSeqNumber());
            if (commandManager.executeCommand(stopCommand)) {
                ManagerAction managerAction = new ManagerAction(
                        stopTask, managerActionType,
                        row, agent, nodeType);
                managerAction.disableButtons();
                actionsCache.put(stopTask.getUuid(), managerAction,
                        stopCommand.getRequest().getTimeout() * 1000 + 2000);

            }
        } else if (managerActionType == ManagerActionType.START_NODE) {

            if (nodeType == NodeType.DATA_NODE) {
                Task startDataNodeTask = Util.createTask("Start data node");
                Command startDataNodeCommand = Commands.getStartNodeCommand();
                startDataNodeCommand.getRequest().setUuid(agent.getUuid());
                startDataNodeCommand.getRequest().setTaskUuid(startDataNodeTask.getUuid());
                startDataNodeCommand.getRequest().setRequestSequenceNumber(startDataNodeTask.getIncrementedReqSeqNumber());
                if (commandManager.executeCommand(startDataNodeCommand)) {
                    ManagerAction managerAction = new ManagerAction(
                            startDataNodeTask,
                            managerActionType,
                            row, agent, nodeType);
                    managerAction.disableButtons();
                    actionsCache.put(startDataNodeTask.getUuid(),
                            managerAction,
                            startDataNodeCommand.getRequest().getTimeout() * 1000 + 2000);
                }

            } else if (nodeType == NodeType.CONFIG_NODE) {
                Task startConfigSvrTask = Util.createTask("Start config server");
                Command startConfigSvrCommand = Commands.getStartConfigServerCommand();
                startConfigSvrCommand.getRequest().setUuid(agent.getUuid());
                startConfigSvrCommand.getRequest().setTaskUuid(startConfigSvrTask.getUuid());
                startConfigSvrCommand.getRequest().setRequestSequenceNumber(startConfigSvrTask.getIncrementedReqSeqNumber());
                if (commandManager.executeCommand(startConfigSvrCommand)) {
                    ManagerAction managerAction = new ManagerAction(
                            startConfigSvrTask,
                            managerActionType,
                            row, agent, nodeType);
                    managerAction.disableButtons();
                    actionsCache.put(startConfigSvrTask.getUuid(),
                            managerAction,
                            startConfigSvrCommand.getRequest().getTimeout() * 1000 + 2000);
                }
            } else if (nodeType == NodeType.ROUTER_NODE) {
                if (clusterInfo != null) {
                    Task startRouterTask = Util.createTask("Start router");
                    StringBuilder configServersArg = new StringBuilder();
                    for (UUID agentUUID : clusterInfo.getConfigServers()) {
                        Agent cfgSrvAgent = agentManager.getAgent(agentUUID);
                        if (cfgSrvAgent != null) {
                            configServersArg.append(cfgSrvAgent.getHostname()).append(Constants.DOMAIN).//use hostname when fixed
                                    append(":").append(Constants.CONFIG_SRV_PORT).append(",");
                        }
                    }
                    //drop comma
                    if (configServersArg.length() > 0) {
                        configServersArg.setLength(configServersArg.length() - 1);
                    }
                    Command startRouterCommand = Commands.getStartRouterCommand(configServersArg.toString());
                    startRouterCommand.getRequest().setUuid(agent.getUuid());
                    startRouterCommand.getRequest().setTaskUuid(startRouterTask.getUuid());
                    startRouterCommand.getRequest().setRequestSequenceNumber(startRouterTask.getIncrementedReqSeqNumber());
                    if (commandManager.executeCommand(startRouterCommand)) {
                        ManagerAction managerAction = new ManagerAction(
                                startRouterTask,
                                managerActionType,
                                row, agent, nodeType);
                        managerAction.disableButtons();
                        actionsCache.put(startRouterTask.getUuid(),
                                managerAction,
                                startRouterCommand.getRequest().getTimeout() * 1000 + 2000);
                    }
                } else {
                    show("Please select cluster!");
                }
            }
        } else if (managerActionType == ManagerActionType.DESTROY_NODE) {
            /*
             Task destroyTask = Util.createTask("Destroy Node");
             //find the primary node in rs
             Agent primaryNode = null;
             //OR we can unregister from primary node on CHECK-STATUS response?
            
             //uninstall mongo
             Command killCmd = Commands.getKillAllCommand();
             killCmd.getRequest().setUuid(agent.getUuid());
             killCmd.getRequest().setTaskUuid(destroyTask.getUuid());
             killCmd.getRequest().setRequestSequenceNumber(destroyTask.getIncrementedReqSeqNumber());
             Command cleanCmd = Commands.getCleanCommand();
             cleanCmd.getRequest().setUuid(agent.getUuid());
             cleanCmd.getRequest().setTaskUuid(destroyTask.getUuid());
             cleanCmd.getRequest().setRequestSequenceNumber(destroyTask.getIncrementedReqSeqNumber());
             Command uninstallCmd = Commands.getUninstallCommand();
             uninstallCmd.getRequest().setUuid(agent.getUuid());
             uninstallCmd.getRequest().setTaskUuid(destroyTask.getUuid());
             uninstallCmd.getRequest().setRequestSequenceNumber(destroyTask.getIncrementedReqSeqNumber());

             //unregister the node from primary
             Command unregisterFromPrimaryCmd = Commands.getUnregisterSecondaryNodeFromPrimaryCommand(agent.getHostname());
             unregisterFromPrimaryCmd.getRequest().setUuid(primaryNode.getUuid());
             unregisterFromPrimaryCmd.getRequest().setTaskUuid(destroyTask.getUuid());
             unregisterFromPrimaryCmd.getRequest().setRequestSequenceNumber(destroyTask.getIncrementedReqSeqNumber());

            
             commandManager.executeCommand(killCmd);
             commandManager.executeCommand(cleanCmd);
             commandManager.executeCommand(uninstallCmd);
             commandManager.executeCommand(unregisterFromPrimaryCmd);

             ManagerAction managerAction = new ManagerAction(
             destroyTask,
             managerActionType,
             row, agent, nodeType);
             managerAction.disableButtons();

             int totalTimeout = killCmd.getRequest().getTimeout()
             + cleanCmd.getRequest().getTimeout() + uninstallCmd.getRequest().getTimeout();

             actionsCache.put(destroyTask.getUuid(),
             managerAction,
             totalTimeout * 1000 + 2000);
             */
        } else if (managerActionType == ManagerActionType.ADD_NODE) {
        }
    }

    @Override
    public void onResponse(Response response) {
        try {

            if (response != null && response.getTaskUuid() != null) {
                ManagerAction managerAction = actionsCache.get(response.getTaskUuid());
                if (managerAction != null) {
                    boolean actionCompleted = false;
                    managerAction.addOutput(response.getStdOut());
                    if (managerAction.getManagerActionType() == ManagerActionType.CHECK_NODE_STATUS) {
                        if (managerAction.getOutput().contains("couldn't connect to server")) {
                            managerAction.enableStartButton();
                            actionCompleted = true;
                        } else if (managerAction.getOutput().
                                contains("connecting to")) {
                            managerAction.enableStopButton();
                            actionCompleted = true;
                        } else if (managerAction.getOutput().contains("mongo: not found")) {
                            /*TODO
                             1) delete from mongo_cluster_info
                             2) delete from UI table
                             3) possibly unregister from primary node here? 
                             */
                            actionCompleted = true;
                        }
                    } else if (managerAction.getManagerActionType() == ManagerActionType.START_NODE) {
                        if (managerAction.getOutput().contains("child process started successfully, parent exiting")) {
                            actionCompleted = true;
                            //add check command to actualize buttons
                            executeManagerAction(ManagerActionType.CHECK_NODE_STATUS,
                                    managerAction.getAgent(), managerAction.getNodeType(),
                                    managerAction.getRow());
                        }
                    } else if (managerAction.getManagerActionType() == ManagerActionType.STOP_NODE) {
                        if (Util.isFinalResponse(response)) {
                            actionCompleted = true;//possibly check exit code here
                            //add check command to actualize buttons
                            executeManagerAction(ManagerActionType.CHECK_NODE_STATUS,
                                    managerAction.getAgent(), managerAction.getNodeType(),
                                    managerAction.getRow());
                        }
                    } else if (managerAction.getManagerActionType() == ManagerActionType.DESTROY_NODE) {

                        processDestroyCommandResponse(response);
                    } else if (managerAction.getManagerActionType() == ManagerActionType.ADD_NODE) {

                        processAddCommandResponse(response);
                    }
                    if (actionCompleted || Util.isFinalResponse(response)) {
                        if (managerAction.getManagerActionType() != ManagerActionType.DESTROY_NODE
                                && managerAction.getManagerActionType() != ManagerActionType.ADD_NODE) {
                            Task task = managerAction.getTask();
                            task.setTaskStatus(actionCompleted ? TaskStatus.SUCCESS : TaskStatus.FAIL);
                            Util.saveTask(task);
                            actionsCache.remove(managerAction.getTask().getUuid());
                        }
                        //since we launch check-status command after any action command 
                        //then action commands shud not hide progress indicator
                        //also destroy button will be reenabled after check-status completion 
                        if (managerAction.getManagerActionType() == ManagerActionType.CHECK_NODE_STATUS) {
                            managerAction.enableDestroyButton();
                            managerAction.hideProgress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in onResponse", e);
        }
    }

    private void processDestroyCommandResponse(Response response) {
        //save task upon completion && launch check-status task 
    }

    private void processAddCommandResponse(Response response) {
        //save task upon completion && launch check-status task 
    }

    @Override
    public String getSource() {
        return MongoModule.MODULE_NAME;
    }

    private void show(String notification) {
        contentRoot.getWindow().showNotification(notification);
    }

    private void populateTable(final Table table, List<UUID> agentUUIDs, final NodeType nodeType) {

        table.removeAllItems();

        for (UUID agentUUID : agentUUIDs) {

            final Agent agent = agentManager.getAgent(agentUUID);
            Button checkBtn = new Button("Check");
            Button startBtn = new Button("Start");
            Button stopBtn = new Button("Stop");
            Button destroyBtn = new Button("Destroy");
            stopBtn.setEnabled(false);
            startBtn.setEnabled(false);

            Object rowId = table.addItem(new Object[]{
                agent.getHostname(),
                checkBtn,
                startBtn,
                stopBtn,
                destroyBtn,
                null},
                    null);

            final Item row = table.getItem(rowId);

            startBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    executeManagerAction(ManagerActionType.START_NODE, agent, nodeType, row);
                }
            });
            stopBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    executeManagerAction(ManagerActionType.STOP_NODE, agent, nodeType, row);
                }
            });
            destroyBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    executeManagerAction(ManagerActionType.DESTROY_NODE, agent, nodeType, row);
                }
            });

            checkBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    executeManagerAction(ManagerActionType.CHECK_NODE_STATUS, agent, nodeType, row);
                }
            });
        }
    }

    private Table createTableTemplate(String caption) {
        Table table = new Table(caption);
        table.addContainerProperty(Constants.TABLE_HOST_PROPERTY, String.class, null);
        table.addContainerProperty(Constants.TABLE_CHECK_PROPERTY, Button.class, null);
        table.addContainerProperty(Constants.TABLE_START_PROPERTY, Button.class, null);
        table.addContainerProperty(Constants.TABLE_STOP_PROPERTY, Button.class, null);
        table.addContainerProperty(Constants.TABLE_DESTROY_PROPERTY, Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(250, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);
        return table;
    }

    private void refreshClustersInfo() {
        List<MongoClusterInfo> mongoClusterInfos = commandManager.getMongoClustersInfo();
        clusterCombo.removeAllItems();
        if (mongoClusterInfos != null) {
            for (MongoClusterInfo mongoClusterInfo : mongoClusterInfos) {
                clusterCombo.addItem(mongoClusterInfo);
                clusterCombo.setItemCaption(mongoClusterInfo,
                        String.format("Name: %s RS: %s", mongoClusterInfo.getClusterName(), mongoClusterInfo.getReplicaSetName()));
            }
        }
    }

    private String getNodePort(NodeType nodeType) {
        if (nodeType == NodeType.CONFIG_NODE) {
            return Constants.CONFIG_SRV_PORT + "";
        } else if (nodeType == NodeType.ROUTER_NODE) {
            return Constants.ROUTER_PORT + "";
        } else {
            return Constants.DATA_NODE_PORT + "";
        }
    }

}
