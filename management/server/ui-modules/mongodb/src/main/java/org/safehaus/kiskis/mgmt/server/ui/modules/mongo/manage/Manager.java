/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

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
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;

/**
 *
 * @author dilshat
 */
public class Manager implements ResponseListener {

    private final VerticalLayout contentRoot;
    private final PersistenceInterface persistenceManager;
    private final CommandManagerInterface commandManager;
    private final ComboBox clusterCombo;
    private final ExpiringCache<UUID, ManagerAction> actionsCache = new ExpiringCache<UUID, ManagerAction>();

    public Manager() {
        //get db and transport managers
        persistenceManager = ServiceLocator.getService(PersistenceInterface.class);
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
        clusterCombo.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() instanceof MongoClusterInfo) {
                    MongoClusterInfo clusterInfo = (MongoClusterInfo) event.getProperty().getValue();
                    populateTable(configServersTable, clusterInfo.getConfigServers(), NodeType.CONFIG_NODE);
                    populateTable(routersTable, clusterInfo.getRouters(), NodeType.ROUTER_NODE);
                    populateTable(dataNodesTable, clusterInfo.getDataNodes(), NodeType.DATA_NODE);
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
        if (response != null && response.getTaskUuid() != null) {
            ManagerAction managerAction = actionsCache.get(response.getTaskUuid());
            if (managerAction != null) {
                if (managerAction.getManagerActionType() == ManagerActionType.CHECK_NODE_STATUS) {
                    managerAction.addOutput(response.getStdOut());
                    if (managerAction.getOutput().
                            matches("connecting to: .*/test")) {
                        managerAction.getStartButton().setEnabled(false);
                        managerAction.getStopButton().setEnabled(true);
                    } else if (managerAction.getOutput().contains("Error: couldn't connect to server ")) {
                        managerAction.getStartButton().setEnabled(true);
                        managerAction.getStopButton().setEnabled(false);
                    } else if (managerAction.getOutput().contains("mongo: not found")) {
                        //disable destroy button
                    }
                }
            }
        }
    }

    @Override
    public String getSource() {
        return MongoModule.MODULE_NAME;
    }

    private void show(String notification) {
        contentRoot.getWindow().showNotification(notification);
    }

    public PersistenceInterface getDbManager() {
        return persistenceManager;
    }

    private void populateTable(Table table, List<UUID> agentUUIDs, NodeType nodeType) {
        table.removeAllItems();
        for (UUID agentUUID : agentUUIDs) {
            final Agent agent = persistenceManager.getAgent(agentUUID);
            final Button checkBtn = new Button("Check");
            final Button startBtn = new Button("Start");
            final Button stopBtn = new Button("Stop");
            final Button destroyBtn = new Button("Destroy");
            checkBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Task checkTask = Util.createTask("Check mongo node status");
                    Command checkCommand = Commands.getCheckInstanceRunningCommand(
                            MessageFormat.format("{0}{1}", agent.getHostname(), Constants.DOMAIN),
                            Constants.CONFIG_SRV_PORT + "");
                    if (commandManager.executeCommand(checkCommand)) {
                        actionsCache.put(checkTask.getUuid(),
                                new ManagerAction(checkTask, startBtn, stopBtn,
                                        ManagerActionType.CHECK_NODE_STATUS),
                                checkCommand.getRequest().getTimeout() + 1000);
                    }
                }
            });
            startBtn.setEnabled(false);
            startBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                }
            });
            stopBtn.setEnabled(false);
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
            table.addItem(new Object[]{
                agent.getHostname(),
                checkBtn,
                startBtn,
                stopBtn,
                destroyBtn},
                    agent);
        }
    }

    private Table createTableTemplate(String caption) {
        Table table = new Table(caption);
        table.addContainerProperty("Host", String.class, null);
        table.addContainerProperty("Check", Button.class, null);
        table.addContainerProperty("Start", Button.class, null);
        table.addContainerProperty("Stop", Button.class, null);
        table.addContainerProperty("Destroy", Button.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(250, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(true);
        table.setImmediate(true);
        return table;
    }

    private void refreshClustersInfo() {
        List<MongoClusterInfo> mongoClusterInfos = persistenceManager.getMongoClustersInfo();
        clusterCombo.removeAllItems();
        if (mongoClusterInfos != null) {
            for (MongoClusterInfo clusterInfo : mongoClusterInfos) {
                clusterCombo.addItem(clusterInfo);
                clusterCombo.setItemCaption(clusterInfo,
                        String.format("Name: %s RS: %s", clusterInfo.getClusterName(), clusterInfo.getReplicaSetName()));
            }
        }
    }

}
