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
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 */
public class Manager implements ResponseListener {

    private static final Logger LOG = Logger.getLogger(Manager.class.getName());

    private final VerticalLayout contentRoot;
    private final CommandManagerInterface commandManager;
    private final AgentManagerInterface agentManager;
    private final ComboBox clusterCombo;
    private final ExpiringCache<UUID, ManagerAction> actionsCache = new ExpiringCache<UUID, ManagerAction>();

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
        clusterCombo.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() instanceof MongoClusterInfo) {
                    MongoClusterInfo clusterInfo = (MongoClusterInfo) event.getProperty().getValue();
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

    @Override
    public void onResponse(Response response) {
        try {

            if (response != null && response.getTaskUuid() != null) {
                ManagerAction managerAction = actionsCache.get(response.getTaskUuid());
                if (managerAction != null) {
                    if (managerAction.getManagerActionType() == ManagerActionType.CHECK_NODE_STATUS) {
                        managerAction.addOutput(response.getStdOut());
                        Button startBtn = managerAction.getItemPropertyValue(Constants.TABLE_START_PROPERTY);
                        Button stopBtn = managerAction.getItemPropertyValue(Constants.TABLE_STOP_PROPERTY);
                        Button destroyBtn = managerAction.getItemPropertyValue(Constants.TABLE_DESTROY_PROPERTY);
                        if (managerAction.getOutput().
                                contains("connecting to")) {
                            startBtn.setEnabled(false);
                            stopBtn.setEnabled(true);
                            destroyBtn.setEnabled(true);
                            actionsCache.remove(managerAction.getTask().getUuid());
                        } else if (managerAction.getOutput().contains("couldn't connect to server")) {
                            stopBtn.setEnabled(false);
                            startBtn.setEnabled(true);
                            destroyBtn.setEnabled(true);
                            actionsCache.remove(managerAction.getTask().getUuid());
                        } else if (managerAction.getOutput().contains("mongo: not found")) {
                            //remove this row
                            Table parentTable = (Table) startBtn.getParent();
                            parentTable.removeItem(managerAction.getRowId());
                            actionsCache.remove(managerAction.getTask().getUuid());
                        }
                    }
                    if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE
                            || response.getType() == ResponseType.EXECUTE_TIMEOUTED) {
                        Task task = managerAction.getTask();
                        task.setTaskStatus(TaskStatus.SUCCESS);
                        Util.saveTask(task);
                        actionsCache.remove(managerAction.getTask().getUuid());
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in onResponse", e);
        }
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
            destroyBtn.setEnabled(false);

            final Object rowId = table.addItem(new Object[]{
                agent.getHostname(),
                checkBtn,
                startBtn,
                stopBtn,
                destroyBtn},
                    null);

            final Item row = table.getItem(rowId);

            startBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                }
            });
            stopBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                }
            });
            destroyBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                }
            });

            checkBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Task checkTask = Util.createTask("Check mongo node status");
                    Command checkCommand = Commands.getCheckInstanceRunningCommand(
                            MessageFormat.format("{0}{1}", agent.getHostname(), Constants.DOMAIN),
                            getNodePort(nodeType));
                    checkCommand.getRequest().setUuid(agent.getUuid());
                    checkCommand.getRequest().setTaskUuid(checkTask.getUuid());
                    checkCommand.getRequest().setRequestSequenceNumber(checkTask.getIncrementedReqSeqNumber());
                    if (commandManager.executeCommand(checkCommand)) {
                        actionsCache.put(checkTask.getUuid(),
                                new ManagerAction(checkTask,
                                        ManagerActionType.CHECK_NODE_STATUS,
                                        row, rowId),
                                checkCommand.getRequest().getTimeout() * 1000 + 2000);
                    }
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
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(250, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(true);
        table.setImmediate(true);
        return table;
    }

    private void refreshClustersInfo() {
        List<MongoClusterInfo> mongoClusterInfos = commandManager.getMongoClustersInfo();
        clusterCombo.removeAllItems();
        if (mongoClusterInfos != null) {
            for (MongoClusterInfo clusterInfo : mongoClusterInfos) {
                clusterCombo.addItem(clusterInfo);
                clusterCombo.setItemCaption(clusterInfo,
                        String.format("Name: %s RS: %s", clusterInfo.getClusterName(), clusterInfo.getReplicaSetName()));
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
