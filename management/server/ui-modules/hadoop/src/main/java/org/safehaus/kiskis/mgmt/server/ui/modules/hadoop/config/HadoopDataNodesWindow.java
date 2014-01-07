package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.util.HadoopCommands;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class HadoopDataNodesWindow extends Window {

    private Button startButton, stopButton, restartButton, addButton;
    private Label statusLabel;
    private AgentsComboBox agentsComboBox;
    private DataNodesTable dataNodesTable;

    private List<String> keys;
    private HadoopClusterInfo cluster;
    private Task addTask, statusTask, configureTask;
    private Agent currentAgent;

    public HadoopDataNodesWindow(String clusterName) {
        setModal(true);
        setCaption("Hadoop Data Node Configuration");

        this.cluster = getCommandManager().getHadoopClusterData(clusterName);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth(900, Sizeable.UNITS_PIXELS);
        verticalLayout.setHeight(500, Sizeable.UNITS_PIXELS);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        buttonLayout.addComponent(getStartButton());
        buttonLayout.addComponent(getStopButton());
        buttonLayout.addComponent(getRestartButton());
        buttonLayout.addComponent(getStatusLabel());

        HorizontalLayout agentsLayout = new HorizontalLayout();
        agentsLayout.setSpacing(true);

        agentsComboBox = new AgentsComboBox(clusterName);
        agentsLayout.addComponent(agentsComboBox);
        agentsLayout.addComponent(getAddButton());

        verticalLayout.addComponent(buttonLayout);
        verticalLayout.addComponent(agentsLayout);
        verticalLayout.addComponent(getTable());
        setContent(verticalLayout);

        getStatus();
    }

    private Button getStartButton() {
        startButton = new Button("Start");

        return startButton;
    }

    private Button getStopButton() {
        stopButton = new Button("Stop");

        return stopButton;
    }

    private Button getRestartButton() {
        restartButton = new Button("Restart");

        return restartButton;
    }

    private Button getAddButton() {
        addButton = new Button("Add");
        addButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cluster = getCommandManager().getHadoopClusterData(cluster.getClusterName());
                Agent agent = (Agent) agentsComboBox.getValue();

                List<UUID> list = new ArrayList<UUID>();
                list.addAll(cluster.getDataNodes());
                list.add(agent.getUuid());

                cluster.setDataNodes(list);
                addButton.setEnabled(false);

                configureNode();
            }
        });

        return addButton;
    }

    private void getStatus() {
        statusTask = RequestUtil.createTask(getCommandManager(), "Get status for Hadoop Data Node");
        Agent master = getAgentManager().getAgent(cluster.getNameNode());

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", master.getUuid().toString());

        RequestUtil.createRequest(getCommandManager(), HadoopCommands.STATUS_DATA_NODE, statusTask, map);
    }

    private void configureNode() {
        configureTask = RequestUtil.createTask(getCommandManager(), "Configuring new node on Hadoop Cluster");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", cluster.getNameNode().toString());
        RequestUtil.createRequest(getCommandManager(), HadoopCommands.COPY_MASTER_KEY, configureTask, map);

        if (!cluster.getNameNode().equals(cluster.getSecondaryNameNode())) {
            map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", cluster.getSecondaryNameNode().toString());
            RequestUtil.createRequest(getCommandManager(), HadoopCommands.COPY_MASTER_KEY, configureTask, map);
        }

        if (!cluster.getJobTracker().equals(cluster.getNameNode()) && !cluster.getJobTracker().equals(cluster.getSecondaryNameNode())) {
            map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", cluster.getJobTracker().toString());
            RequestUtil.createRequest(getCommandManager(), HadoopCommands.COPY_MASTER_KEY, configureTask, map);
        }
    }

    private void addNode() {
        Agent agent = (Agent) agentsComboBox.getValue();
        addTask = RequestUtil.createTask(getCommandManager(), "Adding data node to Hadoop Cluster");

        for (String key : keys) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", agent.getUuid().toString());

            map.put(":PUB_KEY", key);

            RequestUtil.createRequest(getCommandManager(), HadoopCommands.PASTE_MASTER_KEY, addTask, map);
        }

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", agent.getUuid().toString());
        RequestUtil.createRequest(getCommandManager(), HadoopCommands.INSTALL_DEB, addTask, map);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
        map.put(":slave-hostname", agent.getListIP().get(0));
        RequestUtil.createRequest(getCommandManager(), HadoopCommands.ADD_DATA_NODE, addTask, map);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", agent.getUuid().toString());
        RequestUtil.createRequest(getCommandManager(), HadoopCommands.START_DATA_NODE, addTask, map);


        if (!agent.getListIP().isEmpty()) {
            map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
            map.put(":IP", agent.getListIP().get(0));
            RequestUtil.createRequest(getCommandManager(), HadoopCommands.INCLUDE_DATA_NODE, addTask, map);
        }

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
        RequestUtil.createRequest(getCommandManager(), HadoopCommands.REFRESH_DATA_NODES, addTask, map);
    }

    private Label getStatusLabel() {
        statusLabel = new Label();
        statusLabel.setValue("");

        return statusLabel;
    }

    private DataNodesTable getTable() {
        dataNodesTable = new DataNodesTable(cluster.getClusterName());

        return dataNodesTable;
    }

    public void onCommand(Response response) {

        List<ParseResult> list = getCommandManager().parseTask(response.getTaskUuid(), true);
        Task task = getCommandManager().getTask(response.getTaskUuid());

        if (configureTask != null) {
            if (!list.isEmpty() && task.equals(configureTask)) {
                print_r(list);
                if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                    keys = new ArrayList<String>();
                    for (ParseResult pr : list) {
                        keys.add(pr.getResponse().getStdOut().trim());
                    }
                    addNode();
                } else {
                    addButton.setEnabled(true);
                }
                configureTask = null;
            }
        }

        if (addTask != null) {
            if (!list.isEmpty()) {
                if (task.equals(addTask)) {
                    print_r(list);
                    if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                        getCommandManager().saveHadoopClusterData(cluster);

                        agentsComboBox.refreshDataSource();
                        dataNodesTable.refreshDataSource();
                    } else {
                        cluster = getCommandManager().getHadoopClusterData(cluster.getClusterName());
                        agentsComboBox.refreshDataSource();
                    }
                    currentAgent = null;
                    addTask = null;

                    addButton.setEnabled(true);
                }
            }
        }

        if (statusTask != null) {
            if (task.equals(statusTask)) {
                print_r(list);
                for (ParseResult pr : list) {
                    statusLabel.setValue(parseNameNodeStatus(pr.getResponse().getStdOut()));
                }
                statusTask = null;
            }
        }

        dataNodesTable.onCommand(response);
    }

    private String parseNameNodeStatus(String response) {
        String[] array = response.split("\n");
        if (array.length == 4) {
            return array[0]
                    .replaceAll(" (SecondaryNameNode is NOT Running on this machine)", "");
        }

        return "";
    }

    private void print_r(List<ParseResult> list) {
        for (ParseResult p : list) {
            System.out.println("\n");
            System.out.println(p.getRequest());
            System.out.println("\n");
            System.out.println(p.getResponse());
            System.out.println("\n");
        }
    }

    public CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

    public AgentManagerInterface getAgentManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(AgentManagerInterface.class.getName());
            if (serviceReference != null) {
                return AgentManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
