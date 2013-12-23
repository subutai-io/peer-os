package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.HashMap;
import java.util.List;

public final class HadoopDataNodesWindow extends Window {

    public static final String ADD_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop-master-slave.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"slaves\",\":slave-hostname\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String START_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop-daemon.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"start\",\"datanode\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String INCLUDE_NODE = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop-master-slave.sh\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"dfs.include\",\":IP\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String STATUS_CLUSTER = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \"/usr/bin/service\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"hadoop-dfs\",\":command\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    public static final String REFRESH_NODES = "{\n" +
            "\t  \"command\": {\n" +
            "\t    \"type\": \"EXECUTE_REQUEST\",\n" +
            "\t    \"source\": :source,\n" +
            "\t    \"uuid\": :uuid,\n" +
            "\t    \"taskUuid\": :taskUuid,\n" +
            "\t    \"requestSequenceNumber\": :requestSequenceNumber,\n" +
            "\t    \"workingDirectory\": \"/\",\n" +
            "\t    \"program\": \". /etc/profile && hadoop\",\n" +
            "\t    \"stdOut\": \"RETURN\",\n" +
            "\t    \"stdErr\": \"RETURN\",\n" +
            "\t    \"runAs\": \"root\",\n" +
            "\t    \"args\": [\n" +
            "\t      \"dfsadmin\",\"-refreshNodes\"\n" +
            "\t    ],\n" +
            "\t    \"timeout\": 180\n" +
            "\t  }\n" +
            "\t}";

    private Button startButton, stopButton, restartButton;
    private Label statusLabel;
    private AgentsComboBox agentsComboBox;
    private DataNodesTable dataNodesTable;

    private HadoopClusterInfo cluster;
    private Task addTask, statusTask;

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

        statusTask = RequestUtil.createTask(getCommandManager(), "Get status for Hadoop Data Node");
        Agent master = getAgentManager().getAgent(cluster.getNameNode());

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", master.getUuid().toString());
        map.put(":command", "status");

        RequestUtil.createRequest(getCommandManager(), STATUS_CLUSTER, statusTask, map);
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
        Button button = new Button("Add");
        button.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                Agent agent = (Agent) agentsComboBox.getValue();
                cluster = getCommandManager().getHadoopClusterData(cluster.getClusterName());
                cluster.getDataNodes().add(agent.getUuid());

                addTask = RequestUtil.createTask(getCommandManager(), "Adding data node to Hadoop Cluster");

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
                map.put(":slave-hostname", agent.getUuid().toString());
                RequestUtil.createRequest(getCommandManager(), ADD_NODE, addTask, map);

                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());
                RequestUtil.createRequest(getCommandManager(), START_NODE, addTask, map);


                if(!agent.getListIP().isEmpty()){
                    map = new HashMap<String, String>();
                    map.put(":source", HadoopModule.MODULE_NAME);
                    map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
                    map.put(":IP", agent.getListIP().get(0));
                    RequestUtil.createRequest(getCommandManager(), INCLUDE_NODE, addTask, map);
                }

                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
                RequestUtil.createRequest(getCommandManager(), REFRESH_NODES, addTask, map);
            }
        });

        return button;
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
        Task task = getCommandManager().getTask(response.getTaskUuid());
        List<ParseResult> list = getCommandManager().parseTask(task, true);
        task = getCommandManager().getTask(response.getTaskUuid());

        if (addTask != null) {
            if (!list.isEmpty()) {
                if (task.equals(addTask)) {
                    if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                        getCommandManager().saveHadoopClusterData(cluster);

                        agentsComboBox.refreshDataSource();
                        dataNodesTable.refreshDataSource();
                    } else {
                        cluster = getCommandManager().getHadoopClusterData(cluster.getClusterName());
                        agentsComboBox.refreshDataSource();
                    }
                    addTask = null;
                }
            }
        }

        if (statusTask != null) {
            if (task.equals(statusTask)) {
                for (ParseResult pr : list) {
                    statusLabel.setValue(pr.getResponse().getStdOut());
                }
            }
        }

        dataNodesTable.onCommand(response);
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
