package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.tasktracker;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.datanode.AgentsComboBox;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.install.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class TaskTrackersWindow extends Window {

    private Button startButton, stopButton, restartButton, addButton;
    private Label statusLabel;
    private AgentsComboBox agentsComboBox;
    private TaskTrackersTable taskTrackersTable;

    private List<String> keys;
    private HadoopClusterInfo cluster;
    private Task addTask, statusTask, configureTask;
    private Task startTask, stopTask, restartTask;
    private Task readHostsTask, writeHostsTask;
    private Agent currentAgent;

    public TaskTrackersWindow(String clusterName) {
        setModal(true);
        setCaption("Hadoop Job Tracker Configuration");

        this.cluster = getCommandManager().getHadoopClusterData(clusterName);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth(900, Sizeable.UNITS_PIXELS);
        verticalLayout.setHeight(450, Sizeable.UNITS_PIXELS);
        verticalLayout.setSpacing(true);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        buttonLayout.addComponent(getStartButton());
        buttonLayout.addComponent(getStopButton());
        buttonLayout.addComponent(getRestartButton());
        buttonLayout.addComponent(getStatusLabel());

        /*HorizontalLayout agentsLayout = new HorizontalLayout();
        agentsLayout.setSpacing(true);

        agentsComboBox = new AgentsComboBox(clusterName);
        agentsLayout.addComponent(agentsComboBox);
        agentsLayout.addComponent(getAddButton());*/

        Panel panel = new Panel();
        panel.setSizeFull();
        panel.addComponent(buttonLayout);
//        panel.addComponent(agentsLayout);
        panel.addComponent(getTable());

        verticalLayout.addComponent(panel);
        setContent(verticalLayout);

        getStatus();
    }

    private Button getStartButton() {
        startButton = new Button("Start");
        startButton.setEnabled(false);

        startButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                startTask = RequestUtil.createTask(getCommandManager(), "Start Hadoop Cluster Job Tracker");
                Agent master = getAgentManager().getAgent(cluster.getJobTracker());

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", master.getUuid().toString());
                map.put(":command", "start");

                RequestUtil.createRequest(getCommandManager(), Commands.COMMAND_JOB_TRACKER, startTask, map);
                disableButtons(0);
            }
        });

        return startButton;
    }

    private Button getStopButton() {
        stopButton = new Button("Stop");
        stopButton.setEnabled(false);

        stopButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                stopTask = RequestUtil.createTask(getCommandManager(), "Stop Hadoop Cluster Job Tracker");
                Agent master = getAgentManager().getAgent(cluster.getJobTracker());

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", master.getUuid().toString());
                map.put(":command", "stop");

                RequestUtil.createRequest(getCommandManager(), Commands.COMMAND_JOB_TRACKER, stopTask, map);
                disableButtons(0);
            }
        });


        return stopButton;
    }

    private Button getRestartButton() {
        restartButton = new Button("Restart");
        restartButton.setEnabled(false);

        restartButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                restartTask = RequestUtil.createTask(getCommandManager(), "Restart Hadoop Cluster Job Tracker");
                Agent master = getAgentManager().getAgent(cluster.getJobTracker());

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", master.getUuid().toString());
                map.put(":command", "restart");

                RequestUtil.createRequest(getCommandManager(), Commands.COMMAND_JOB_TRACKER, restartTask, map);
                disableButtons(0);
            }
        });

        return restartButton;
    }

    private void disableButtons(int status) {
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        restartButton.setEnabled(false);

        if (status == 1) {
            stopButton.setEnabled(true);
            restartButton.setEnabled(true);
        }

        if (status == 2) {
            startButton.setEnabled(true);
        }
    }

    private Button getAddButton() {
        addButton = new Button("Add");
        addButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (configureTask == null) {
                    cluster = getCommandManager().getHadoopClusterData(cluster.getClusterName());
                    Agent agent = (Agent) agentsComboBox.getValue();

                    List<UUID> list = new ArrayList<UUID>();
                    list.addAll(cluster.getDataNodes());
                    list.add(agent.getUuid());

                    cluster.setDataNodes(list);
                    addButton.setEnabled(false);

                    configureNode();
                }
            }
        });

        return addButton;
    }

    private void getStatus() {
        statusTask = RequestUtil.createTask(getCommandManager(), "Get status for Hadoop Task Tracker");
        Agent master = getAgentManager().getAgent(cluster.getJobTracker());

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", master.getUuid().toString());

        RequestUtil.createRequest(getCommandManager(), Commands.STATUS_DATA_NODE, statusTask, map);

        taskTrackersTable.refreshDataSource();
    }

    private void configureNode() {
        configureTask = RequestUtil.createTask(getCommandManager(), "Configuring new node on Hadoop Cluster");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", cluster.getNameNode().toString());
        RequestUtil.createRequest(getCommandManager(), Commands.COPY_MASTER_KEY, configureTask, map);

        if (!cluster.getNameNode().equals(cluster.getSecondaryNameNode())) {
            map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", cluster.getSecondaryNameNode().toString());
            RequestUtil.createRequest(getCommandManager(), Commands.COPY_MASTER_KEY, configureTask, map);
        }

        if (!cluster.getJobTracker().equals(cluster.getNameNode()) && !cluster.getJobTracker().equals(cluster.getSecondaryNameNode())) {
            map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", cluster.getJobTracker().toString());
            RequestUtil.createRequest(getCommandManager(), Commands.COPY_MASTER_KEY, configureTask, map);
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

            RequestUtil.createRequest(getCommandManager(), Commands.PASTE_MASTER_KEY, addTask, map);
        }

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", agent.getUuid().toString());
        RequestUtil.createRequest(getCommandManager(), Commands.INSTALL_DEB, addTask, map);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
        map.put(":slave-hostname", agent.getHostname());
        RequestUtil.createRequest(getCommandManager(), Commands.ADD_DATA_NODE, addTask, map);

        for (UUID uuid : cluster.getDataNodes()) {
            Agent agentDataNode = getAgentManager().getAgent(uuid);
            map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
            map.put(":IP", agentDataNode.getHostname());
            RequestUtil.createRequest(getCommandManager(), Commands.INCLUDE_DATA_NODE, addTask, map);
        }

        /*map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", agent.getUuid().toString());
//        map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
        RequestUtil.createRequest(getCommandManager(), Commands.START_DATA_NODE, addTask, map);*/
    }

    public void readHosts() {
        if (readHostsTask == null) {
            readHostsTask = RequestUtil.createTask(getCommandManager(), "Read /etc/hosts file");

            for (UUID uuid : getAllNodes()) {
                Agent agent = getAgentManager().getAgent(uuid);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());
                RequestUtil.createRequest(getCommandManager(), Commands.READ_HOSTNAME, readHostsTask, map);
            }
        }
    }

    public void writeHosts(List<ParseResult> list) {
        if (writeHostsTask == null) {
            writeHostsTask = RequestUtil.createTask(getCommandManager(), "Write /etc/hosts file");

            for (ParseResult pr : list) {
                Agent agent = getAgentManager().getAgent(pr.getRequest().getUuid());
                String hosts = editHosts(pr.getResponse().getStdOut(), agent);

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());
                map.put(":hosts", hosts);
                RequestUtil.createRequest(getCommandManager(), Commands.WRITE_HOSTNAME, writeHostsTask, map);
            }

            /*HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
            RequestUtil.createRequest(getCommandManager(), Commands.REFRESH_DATA_NODES, writeHostsTask, map);*/
        }
    }

    private String editHosts(String input, Agent localAgent) {
        StringBuilder result = new StringBuilder();

        String[] hosts = input.split("\n");
        for (String host : hosts) {
            host = host.trim();
            boolean isContains = false;
            for (UUID uuid : getAllNodes()) {
                Agent agent = getAgentManager().getAgent(uuid);
                if (host.contains(agent.getHostname()) ||
                        host.contains("localhost") ||
                        host.contains(localAgent.getHostname()) ||
                        host.contains(localAgent.getListIP().get(0))) {
                    isContains = true;
                }
            }

            if (!isContains) {
                result.append(host);
                result.append("\n");
            }
        }

        for (UUID uuid : getAllNodes()) {
            Agent agent = getAgentManager().getAgent(uuid);
            result.append(agent.getListIP().get(0));
            result.append("\t");
            result.append(agent.getHostname());
            result.append(".");
            result.append(cluster.getIpMask());
            result.append("\t");
            result.append(agent.getHostname());
            result.append("\n");
        }

        result.append("127.0.0.1\tlocalhost");

        return result.toString();
    }

    private Label getStatusLabel() {
        statusLabel = new Label();
        statusLabel.setValue("");

        return statusLabel;
    }

    private List<UUID> getAllNodes() {
        List<UUID> list = new ArrayList<UUID>();
        list.addAll(cluster.getDataNodes());
        list.addAll(cluster.getTaskTrackers());
        list.add(cluster.getNameNode());
        list.add(cluster.getJobTracker());
        list.add(cluster.getSecondaryNameNode());

        return list;
    }

    private TaskTrackersTable getTable() {
        taskTrackersTable = new TaskTrackersTable(cluster.getClusterName());

        return taskTrackersTable;
    }

    public void onCommand(Response response) {

        List<ParseResult> list = getCommandManager().parseTask(response.getTaskUuid(), true);
        Task task = getCommandManager().getTask(response.getTaskUuid());

        if (configureTask != null) {
            if (!list.isEmpty() && task.equals(configureTask)) {
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
                    if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                        readHosts();
                    }
                    currentAgent = null;
                    addTask = null;
                }
            }
        }

        if (statusTask != null) {
            if (task.equals(statusTask)) {
                for (ParseResult pr : list) {
                    String status = parseNameNodeStatus(pr.getResponse().getStdOut());
                    statusLabel.setValue(status);
                    if (status.trim().equalsIgnoreCase("Running")) {
                        disableButtons(1);
                    } else {
                        disableButtons(2);
                    }
                }
                statusTask = null;
            }
        }

        if (startTask != null) {
            if (task.equals(startTask)) {
                getStatus();
                startTask = null;
            }
        }

        if (stopTask != null) {
            if (task.equals(stopTask)) {
                getStatus();
                stopTask = null;
            }
        }

        if (restartTask != null) {
            if (task.equals(restartTask)) {
                getStatus();
                restartTask = null;
            }
        }

        if (readHostsTask != null) {
            if (task.equals(readHostsTask)) {
                if (task.getTaskStatus().equals(TaskStatus.SUCCESS)) {
                    writeHosts(list);
                    readHostsTask = null;
                }
            }
        }

        if (writeHostsTask != null && task.equals(writeHostsTask)) {
            if (task.getTaskStatus().equals(TaskStatus.SUCCESS)) {
                getCommandManager().saveHadoopClusterData(cluster);
                agentsComboBox.refreshDataSource();
                taskTrackersTable.refreshDataSource();
            } else {
                agentsComboBox.refreshDataSource();
            }

            writeHostsTask = null;
            addButton.setEnabled(true);
        }

        taskTrackersTable.onCommand(response);
    }

    private String parseNameNodeStatus(String response) {
        String[] array = response.split("\n");

        for (String status : array) {
            if (status.contains("JobTracker")) {
                return status.replaceAll("JobTracker is ", "");
            }
        }

        return "";
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
