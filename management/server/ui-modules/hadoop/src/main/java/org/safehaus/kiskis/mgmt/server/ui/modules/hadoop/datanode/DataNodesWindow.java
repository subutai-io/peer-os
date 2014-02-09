package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.datanode;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.install.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;

public final class DataNodesWindow extends Window {

    private Button startButton, stopButton, restartButton, addButton;
    private Label statusLabel;
    private AgentsComboBox agentsComboBox;
    private DataNodesTable dataNodesTable;

    private List<String> keys;
    private HadoopClusterInfo cluster;
    private Task addTask, statusTask, configureTask;
    private Task startTask, stopTask, restartTask;
    private Task hadoopReadHosts;
    private Task hadoopWriteHosts;
//    private Agent currentAgent;

    public DataNodesWindow(String clusterName) {
        setModal(true);
        setCaption("Hadoop Data Node Configuration");

        this.cluster = HadoopDAO.getHadoopClusterInfo(clusterName);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth(900, Sizeable.UNITS_PIXELS);
        verticalLayout.setHeight(450, Sizeable.UNITS_PIXELS);

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
                startTask = RequestUtil.createTask("Start Hadoop Cluster");
                Agent master = getAgentManager().getAgentByUUID(cluster.getNameNode());

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", master.getUuid().toString());
                map.put(":command", "start");

                RequestUtil.createRequest(getCommandManager(), Commands.COMMAND_NAME_NODE, startTask, map);
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
                stopTask = RequestUtil.createTask("Stop Hadoop Cluster");
                Agent master = getAgentManager().getAgentByUUID(cluster.getNameNode());

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", master.getUuid().toString());
                map.put(":command", "stop");

                RequestUtil.createRequest(getCommandManager(), Commands.COMMAND_NAME_NODE, stopTask, map);
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
                restartTask = RequestUtil.createTask("Start Hadoop Cluster");
                Agent master = getAgentManager().getAgentByUUID(cluster.getNameNode());

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", master.getUuid().toString());
                map.put(":command", "restart");

                RequestUtil.createRequest(getCommandManager(), Commands.COMMAND_NAME_NODE, restartTask, map);
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
                    cluster = HadoopDAO.getHadoopClusterInfo(cluster.getClusterName());
                    Agent agent = (Agent) agentsComboBox.getValue();

//                cluster.getDataNodes().add(agent.getUuid());
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
        statusTask = RequestUtil.createTask("Get status for Hadoop Data Node");
        Agent master = getAgentManager().getAgentByUUID(cluster.getNameNode());

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", master.getUuid().toString());

        RequestUtil.createRequest(getCommandManager(), Commands.STATUS_DATA_NODE, statusTask, map);

        dataNodesTable.refreshDataSource();
    }

    private void configureNode() {
        configureTask = RequestUtil.createTask("Configuring new node on Hadoop Cluster");

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
        addTask = RequestUtil.createTask("Adding data node to Hadoop Cluster");

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
        map.put(":uuid", getAgentManager().getAgentByUUID(cluster.getNameNode()).getUuid().toString());
        map.put(":slave-hostname", agent.getHostname());
        RequestUtil.createRequest(getCommandManager(), Commands.ADD_DATA_NODE, addTask, map);

        for (UUID uuid : cluster.getDataNodes()) {
            Agent agentDataNode = getAgentManager().getAgentByUUID(uuid);
            map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", getAgentManager().getAgentByUUID(cluster.getNameNode()).getUuid().toString());
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
        if (hadoopReadHosts == null) {
            hadoopReadHosts = RequestUtil.createTask("Read /etc/hosts file");

            for (UUID uuid : getAllNodes()) {
                Agent agent = getAgentManager().getAgentByUUID(uuid);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());
                RequestUtil.createRequest(getCommandManager(), Commands.READ_HOSTNAME, hadoopReadHosts, map);
            }
        }
    }

    public void writeHosts(List<ParseResult> list) {
        if (hadoopWriteHosts == null) {
            hadoopWriteHosts = RequestUtil.createTask("Write /etc/hosts file");

            for (ParseResult pr : list) {
                Agent agent = getAgentManager().getAgentByUUID(pr.getRequest().getUuid());
                String hosts = editHosts(pr.getResponse().getStdOut(), agent);

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", agent.getUuid().toString());
                map.put(":hosts", hosts);
                RequestUtil.createRequest(getCommandManager(), Commands.WRITE_HOSTNAME, hadoopWriteHosts, map);
            }

            /*HashMap<String, String> map = new HashMap<String, String>();
             map.put(":source", HadoopModule.MODULE_NAME);
             map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
             RequestUtil.createRequest(getCommandManager(), Commands.REFRESH_DATA_NODES, hadoopWriteHosts, map);*/
        }
    }

    private String editHosts(String input, Agent localAgent) {
        StringBuilder result = new StringBuilder();

        String[] hosts = input.split("\n");
        for (String host : hosts) {
            host = host.trim();
            boolean isContains = false;
            for (UUID uuid : getAllNodes()) {
                Agent agent = getAgentManager().getAgentByUUID(uuid);
                if (host.contains(agent.getHostname())
                        || host.contains("localhost")
                        || host.contains(localAgent.getHostname())
                        || host.contains(localAgent.getListIP().get(0))) {
                    isContains = true;
                }
            }

            if (!isContains) {
                result.append(host);
                result.append("\n");
            }
        }

        for (UUID uuid : getAllNodes()) {
            Agent agent = getAgentManager().getAgentByUUID(uuid);
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

    private DataNodesTable getTable() {
        dataNodesTable = new DataNodesTable(cluster.getClusterName());

        return dataNodesTable;
    }

    public void onCommand(Response response) {

        List<ParseResult> list = RequestUtil.parseTask(response.getTaskUuid(), true);
        Task task = RequestUtil.getTask(response.getTaskUuid());

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
//                    currentAgent = null;
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

        if (hadoopReadHosts != null) {
            if (task.equals(hadoopReadHosts)) {
                if (task.getTaskStatus().equals(TaskStatus.SUCCESS)) {
                    writeHosts(list);
                    hadoopReadHosts = null;
                }
            }
        }

        if (hadoopWriteHosts != null && task.equals(hadoopWriteHosts)) {
            if (task.getTaskStatus().equals(TaskStatus.SUCCESS)) {
                HadoopDAO.saveHadoopClusterInfo(cluster);
                agentsComboBox.refreshDataSource();
                dataNodesTable.refreshDataSource();
            } else {
                agentsComboBox.refreshDataSource();
            }

            hadoopWriteHosts = null;
            addButton.setEnabled(true);
        }

        dataNodesTable.onCommand(response);
    }

    private String parseNameNodeStatus(String response) {
        String[] array = response.split("\n");

        for (String status : array) {
            if (status.contains("NameNode")) {
                return status.replaceAll("NameNode is ", "")
                        .replaceAll("\\(SecondaryNOT Running on this machine\\)", "");
            }
        }

        return "";
    }

    public CommandManager getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManager.class.getName());
            if (serviceReference != null) {
                return CommandManager.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

    public AgentManager getAgentManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(AgentManager.class.getName());
            if (serviceReference != null) {
                return AgentManager.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
