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
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Commands;
import org.safehaus.kiskis.mgmt.server.ui.ConfirmationDialogCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
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
     1) add node
     2) destroy cluster
    
     */

    private static final Logger LOG = Logger.getLogger(Manager.class.getName());

    private final VerticalLayout contentRoot;
    private final CommandManagerInterface commandManager;
    private final AgentManagerInterface agentManager;
    private final ComboBox clusterCombo;
    private final ExpiringCache<UUID, Action> actionsCache = new ExpiringCache<UUID, Action>();
    private MongoClusterInfo clusterInfo;
    private final Table configServersTable;
    private final Table routersTable;
    private final Table dataNodesTable;
    private DestroyWindow destroyWindow;

    public Manager(final CustomComponent component) {
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
        configServersTable = createTableTemplate("Config Servers");
        routersTable = createTableTemplate("Query Routers");
        dataNodesTable = createTableTemplate("Data Nodes");
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

        Button checkAllBtn = new Button("Check all");
        checkAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (clusterInfo != null) {
                    checkNodesStatus(configServersTable, NodeType.CONFIG_NODE);
                    checkNodesStatus(routersTable, NodeType.ROUTER_NODE);
                    checkNodesStatus(dataNodesTable, NodeType.DATA_NODE);
                } else {
                    show("Please, select cluster");
                }
            }

        });

        topContent.addComponent(checkAllBtn);

        Button destroyClusterBtn = new Button("Destroy cluster");
        destroyClusterBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (clusterInfo != null) {
                    MgmtApplication.showConfirmationDialog(
                            "Cluster destruction confirmation",
                            String.format("Do you want to destroy the %s cluster?", clusterInfo.getClusterName()),
                            "Yes", "No", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (ok) {
                                        Set clusterMembers = new HashSet<Agent>();
                                        for (UUID agentUUID : clusterInfo.getConfigServers()) {
                                            Agent agent = agentManager.getAgent(agentUUID);
                                            if (agent != null) {
                                                clusterMembers.add(agent);
                                            }
                                        }
                                        for (UUID agentUUID : clusterInfo.getRouters()) {
                                            Agent agent = agentManager.getAgent(agentUUID);
                                            if (agent != null) {
                                                clusterMembers.add(agent);
                                            }
                                        }
                                        for (UUID agentUUID : clusterInfo.getDataNodes()) {
                                            Agent agent = agentManager.getAgent(agentUUID);
                                            if (agent != null) {
                                                clusterMembers.add(agent);
                                            }
                                        }
                                        destroyWindow = new DestroyWindow(
                                                "Destroy Mongo Cluster", clusterMembers);
                                        MgmtApplication.addCustomWindow(destroyWindow);
                                        destroyWindow.startUninstallation();
                                    }
                                }
                            });
                } else {
                    show("Please, select cluster");
                }
            }

        });

        topContent.addComponent(destroyClusterBtn);

        content.addComponent(topContent);

        HorizontalLayout midContent = new HorizontalLayout();
        midContent.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        midContent.addComponent(configServersTable);

        midContent.addComponent(routersTable);

        content.addComponent(midContent);

        content.addComponent(dataNodesTable);

        refreshClustersInfo();
    }

    private void checkNodesStatus(Table table, NodeType nodeType) {
        for (Iterator it = table.getItemIds().iterator(); it.hasNext();) {
            int rowId = (Integer) it.next();
            Item row = table.getItem(rowId);
            String hostname = (String) (row.getItemProperty(Constants.TABLE_HOST_PROPERTY).getValue());
            Agent agent = agentManager.getAgentByHostname(hostname);
            if (agent != null) {
                executeManagerAction(ActionType.CHECK_NODE_STATUS,
                        agent, nodeType,
                        row);
            }
        }
    }

    public Component getContent() {
        return contentRoot;
    }

    private void executeManagerAction(ActionType actionType, Agent agent, NodeType nodeType, Item row) {

        if (actionType == ActionType.CHECK_NODE_STATUS) {

            Task checkTask = Util.createTask("Check mongo node status");
            bindCmdToAgentNTask(
                    Commands.getCheckInstanceRunningCommand(
                            MessageFormat.format("{0}{1}", agent.getHostname(), Constants.DOMAIN),
                            getNodePort(nodeType)),
                    agent, checkTask);
            if (commandManager.executeCommand(checkTask.getNextCommand())) {
                Action action = new Action(
                        checkTask, actionType,
                        row, agent, nodeType);
                cacheAction(action, checkTask.getTotalTimeout() * 1000 + 2000);
            }
        } else if (actionType == ActionType.STOP_NODE) {

            Task stopTask = Util.createTask("Stop mongo node");
            bindCmdToAgentNTask(Commands.getStopNodeCommand(), agent, stopTask);
            if (commandManager.executeCommand(stopTask.getNextCommand())) {
                Action action = new Action(
                        stopTask, actionType,
                        row, agent, nodeType);
                cacheAction(action, stopTask.getTotalTimeout() * 1000 + 2000);
            }
        } else if (actionType == ActionType.START_NODE) {

            if (nodeType == NodeType.DATA_NODE) {
                Task startDataNodeTask = Util.createTask("Start data node");
                bindCmdToAgentNTask(Commands.getStartNodeCommand(), agent, startDataNodeTask);
                if (commandManager.executeCommand(startDataNodeTask.getNextCommand())) {
                    Action action = new Action(
                            startDataNodeTask,
                            actionType,
                            row, agent, nodeType);
                    cacheAction(action, startDataNodeTask.getTotalTimeout() * 1000 + 2000);
                }

            } else if (nodeType == NodeType.CONFIG_NODE) {
                Task startConfigSvrTask = Util.createTask("Start config server");
                bindCmdToAgentNTask(Commands.getStartConfigServerCommand(), agent, startConfigSvrTask);
                if (commandManager.executeCommand(startConfigSvrTask.getNextCommand())) {
                    Action action = new Action(
                            startConfigSvrTask,
                            actionType,
                            row, agent, nodeType);
                    cacheAction(action, startConfigSvrTask.getTotalTimeout() * 1000 + 2000);
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
                    bindCmdToAgentNTask(Commands.getStartRouterCommand(configServersArg.toString()),
                            agent, startRouterTask);
                    if (commandManager.executeCommand(startRouterTask.getNextCommand())) {
                        Action action = new Action(
                                startRouterTask,
                                actionType,
                                row, agent, nodeType);
                        cacheAction(action, startRouterTask.getTotalTimeout() * 1000 + 2000);
                    }
                } else {
                    show("Please select cluster!");
                }
            }
        } else if (actionType == ActionType.DESTROY_NODE) {
            if (nodeType == NodeType.CONFIG_NODE) {
                //uninstall mongo
                //restart routers passing the rest of config servers
                Task destroyTask = Util.createTask("Destroy config node");
                //add cleanup commands
                addCleanupCommands(agent, destroyTask);
                //add restart-routers command
                StringBuilder configServersArg = new StringBuilder();
                for (UUID agentUUID : clusterInfo.getConfigServers()) {
                    //skip the node being destroyed
                    if (agentUUID.compareTo(agent.getUuid()) != 0) {
                        Agent cfgSrvAgent = agentManager.getAgent(agentUUID);
                        if (cfgSrvAgent != null) {
                            configServersArg.append(cfgSrvAgent.getHostname()).append(Constants.DOMAIN).//use hostname when fixed
                                    append(":").append(Constants.CONFIG_SRV_PORT).append(",");
                        }
                    }
                }
                //drop comma
                if (configServersArg.length() > 0) {
                    configServersArg.setLength(configServersArg.length() - 1);
                }

                //select routers
                List<Agent> routers = new ArrayList<Agent>();
                for (UUID agentUUID : clusterInfo.getRouters()) {
                    Agent router = agentManager.getAgent(agentUUID);
                    if (router != null) {
                        routers.add(router);
                    }
                }
                for (Agent router : routers) {
                    bindCmdToAgentNTask(Commands.getRestartRouterCommand(configServersArg.toString()),
                            router, destroyTask);
                }

                //kill
                commandManager.executeCommand(destroyTask.getNextCommand());
                //clean
                commandManager.executeCommand(destroyTask.getNextCommand());
                //uninstall
                commandManager.executeCommand(destroyTask.getNextCommand());
                //restart routers
                for (Agent router : routers) {
                    commandManager.executeCommand(destroyTask.getNextCommand());
                }

                Action action = new Action(
                        destroyTask,
                        actionType,
                        row, agent, nodeType);
                cacheAction(action, destroyTask.getTotalTimeout() * 1000 + 2000);

            } else if (nodeType == NodeType.ROUTER_NODE) {
                //uninstall mongo
                Task destroyTask = Util.createTask("Destroy router node");
                //add cleanup commands
                addCleanupCommands(agent, destroyTask);

                //kill
                commandManager.executeCommand(destroyTask.getNextCommand());
                //clean
                commandManager.executeCommand(destroyTask.getNextCommand());
                //uninstall
                commandManager.executeCommand(destroyTask.getNextCommand());

                Action action = new Action(
                        destroyTask,
                        actionType,
                        row, agent, nodeType);
                cacheAction(action, destroyTask.getTotalTimeout() * 1000 + 2000);
            } else if (nodeType == NodeType.DATA_NODE) {
                //uninstall mongo
                //unregister from primary node if this node is not primary otherwise no-op

                Task destroyTask = Util.createTask("Destroy data node");
                //find primary node command
                bindCmdToAgentNTask(Commands.getFindPrimaryNodeCommand(), agent, destroyTask);
                //on response set UUID with primary node agent uuid
                bindCmdToAgentNTask(
                        Commands.getUnregisterSecondaryNodeFromPrimaryCommand(
                                String.format("%s%s", agent.getHostname(), Constants.DOMAIN)),
                        agent, destroyTask);
                //add cleanup commands mongo
                addCleanupCommands(agent, destroyTask);

                if (commandManager.executeCommand(destroyTask.getNextCommand())) {
                    Action action = new Action(
                            destroyTask,
                            actionType,
                            row, agent, nodeType);
                    cacheAction(action, destroyTask.getTotalTimeout() * 1000 + 2000);
                }
            }
        } else if (actionType == ActionType.ADD_NODE) {
        }
    }

    private void addCleanupCommands(Agent agent, Task destroyTask) {
        Command killCmd = Commands.getKillAllCommand();
        bindCmdToAgentNTask(killCmd, agent, destroyTask);
        Command cleanCmd = Commands.getCleanCommand();
        bindCmdToAgentNTask(cleanCmd, agent, destroyTask);
        Command uninstallCmd = Commands.getUninstallCommand();
        bindCmdToAgentNTask(uninstallCmd, agent, destroyTask);
    }

    private void cacheAction(Action action, int timeout) {
        actionsCache.put(action.getTask().getUuid(),
                action,
                timeout);
    }

    private void bindCmdToAgentNTask(Command cmd, Agent agent, Task task) {
        cmd.getRequest().setUuid(agent.getUuid());
        cmd.getRequest().setTaskUuid(task.getUuid());
        cmd.getRequest().setRequestSequenceNumber(task.getIncrementedReqSeqNumber());
        task.addCommand(cmd);
    }

    @Override
    public void onResponse(Response response) {
        try {

            if (response != null && response.getTaskUuid() != null) {
                Action action = actionsCache.get(response.getTaskUuid());
                if (action != null) {
                    boolean succeeded = false;
                    action.addStdOutput(response.getStdOut());
                    action.addErrOutput(response.getStdErr());
                    if (action.getActionType() == ActionType.CHECK_NODE_STATUS) {
                        if (action.getStdOutput().contains("couldn't connect to server")) {
                            action.enableStartButton();
                            succeeded = true;
                        } else if (action.getStdOutput().
                                contains("connecting to")) {
                            action.enableStopButton();
                            succeeded = true;
                        } else if (action.getErrOutput().contains("mongo: not found")) {
//                            removeNode(action);
                        }
                    } else if (action.getActionType() == ActionType.START_NODE) {
                        if (action.getStdOutput().contains("child process started successfully, parent exiting")) {
                            succeeded = true;
                            checkNodeStatus(action);
                        } else if (Util.isFinalResponse(response)) {
                            checkNodeStatus(action);
                        }
                    } else if (action.getActionType() == ActionType.STOP_NODE) {
                        if (Util.isFinalResponse(response)) {
                            succeeded = response.getExitCode() != null && response.getExitCode() == 0;
                            checkNodeStatus(action);
                        }
                    } else if (action.getActionType() == ActionType.DESTROY_NODE) {

                        processDestroyCommandResponse(action, response);
                    } else if (action.getActionType() == ActionType.ADD_NODE) {

                        processAddCommandResponse(action, response);
                    }
                    if (succeeded || Util.isFinalResponse(response)) {
                        if (action.getActionType() != ActionType.DESTROY_NODE
                                && action.getActionType() != ActionType.ADD_NODE) {
                            Task task = action.getTask();
                            task.setTaskStatus(succeeded ? TaskStatus.SUCCESS : TaskStatus.FAIL);
                            Util.saveTask(task);
                            actionsCache.remove(action.getTask().getUuid());
                            if (!succeeded) {
                                show(String.format("Failed:\n%s\n%s", action.getStdOutput(), action.getErrOutput()));
                            }
                        }
                        //since we launch check-status command after any action command 
                        //then action commands shud not hide progress indicator
                        //also destroy button will be reenabled after check-status completion 
                        if (action.getActionType() == ActionType.CHECK_NODE_STATUS) {
                            try {
                                action.enableDestroyButton();
                                action.hideProgress();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in onResponse", e);
        }
    }

    private void checkNodeStatus(Action action) {
        //add check command to actualize buttons
        executeManagerAction(ActionType.CHECK_NODE_STATUS,
                action.getAgent(), action.getNodeType(),
                action.getRow());
    }

    private void removeNode(Action action) {
        try {
            if (action.getNodeType() == NodeType.CONFIG_NODE) {
                configServersTable.removeItem(action.getRowId());
                List<UUID> nodes = new ArrayList<UUID>(clusterInfo.getConfigServers());
                nodes.remove(action.getAgent().getUuid());
                clusterInfo.setConfigServers(nodes);
                commandManager.saveMongoClusterInfo(clusterInfo);
            } else if (action.getNodeType() == NodeType.ROUTER_NODE) {
                routersTable.removeItem(action.getRowId());
                List<UUID> nodes = new ArrayList<UUID>(clusterInfo.getRouters());
                nodes.remove(action.getAgent().getUuid());
                clusterInfo.setRouters(nodes);
                commandManager.saveMongoClusterInfo(clusterInfo);
            } else if (action.getNodeType() == NodeType.DATA_NODE) {
                dataNodesTable.removeItem(action.getRowId());
                List<UUID> nodes = new ArrayList<UUID>(clusterInfo.getDataNodes());
                nodes.remove(action.getAgent().getUuid());
                clusterInfo.setDataNodes(nodes);
                commandManager.saveMongoClusterInfo(clusterInfo);
            }
        } catch (Exception e) {
        }
    }

    private void processDestroyCommandResponse(Action action, Response response) throws InterruptedException {
        //save task upon completion && launch check-status task 
        if (Util.isFinalResponse(response)) {
            if (action.getErrOutput().contains("mongo: not found")) {
                Task task = action.getTask();
                task.setTaskStatus(TaskStatus.FAIL);
                Util.saveTask(task);
                actionsCache.remove(action.getTask().getUuid());
                removeNode(action);
                show(String.format("Failed:\n%s\n%s", action.getStdOutput(), action.getErrOutput()));
            } else {
                action.incrementResponseCount();
                //this is the first command of destroy data node action -> findPrimaryNode
                if (action.getResponseCount() == 1
                        && action.getNodeType() == NodeType.DATA_NODE) {
                    Pattern p = Pattern.compile("primary\" : \"(.*)\"");
                    Matcher m = p.matcher(action.getStdOutput());
                    Agent primaryNodeAgent = null;
                    if (m.find()) {
                        String primaryNodeHost = m.group(1);
                        if (!Util.isStringEmpty(primaryNodeHost)) {
                            String hostname = primaryNodeHost.split(":")[0].replace(Constants.DOMAIN, "");
                            primaryNodeAgent = agentManager.getAgentByHostname(hostname);
                        }
                    }
                    if (primaryNodeAgent != null) {
                        //unregister the node from primary node if this node is not primary itself
                        Command unregisterFromPrimaryCmd = action.getTask().getNextCommand();
                        unregisterFromPrimaryCmd.getRequest().setUuid(primaryNodeAgent.getUuid());
                        if (primaryNodeAgent.getUuid().compareTo(action.getAgent().getUuid()) != 0) {
                            commandManager.executeCommand(unregisterFromPrimaryCmd);
                            //sleep to let the node complete unregistration
                            Thread.sleep(3000);
                        }
                        //continue cleaning up
                        //kill
                        commandManager.executeCommand(action.getTask().getNextCommand());
                        //clean
                        commandManager.executeCommand(action.getTask().getNextCommand());
                        //uninstall
                        commandManager.executeCommand(action.getTask().getNextCommand());

                    } else {
                        //abort operation
                        show("Could not determine primary node!");
                        Task task = action.getTask();
                        task.setTaskStatus(TaskStatus.FAIL);
                        Util.saveTask(task);
                        actionsCache.remove(action.getTask().getUuid());
                        action.enableDestroyButton();
                        action.hideProgress();
                    }
                } else {
                    //this is the last command
                    if (action.getResponseCount() == action.getTask().getCurrentCommandOrderId()) {
                        Task task = action.getTask();
                        task.setTaskStatus(TaskStatus.SUCCESS);
                        Util.saveTask(task);
                        actionsCache.remove(action.getTask().getUuid());
                        //add check command to actualize buttons
//                        checkNodeStatus(action);
                        removeNode(action);
                    }
                }
            }
        }
    }

    private void processAddCommandResponse(Action action, Response response) {
        //save task upon completion && launch check-status task 
    }

//    @Override
//    public String getSource() {
//        return MongoModule.MODULE_NAME;
//    }
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
            destroyBtn.setData(rowId);

            startBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    executeManagerAction(ActionType.START_NODE, agent, nodeType, row);
                }
            });
            stopBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    executeManagerAction(ActionType.STOP_NODE, agent, nodeType, row);
                }
            });
            destroyBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    executeManagerAction(ActionType.DESTROY_NODE, agent, nodeType, row);
                }
            });

            checkBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    executeManagerAction(ActionType.CHECK_NODE_STATUS, agent, nodeType, row);
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
