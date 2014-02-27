package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.datanode;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.install.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.TaskType;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.TaskUtil;

public final class DataNodesWindow extends Window implements TaskCallback {

    private Button startButton, stopButton, restartButton, addButton;
    private Label statusLabel;
    private AgentsComboBox agentsComboBox;
    private DataNodesTable dataNodesTable;

    private List<String> keys;
    private HadoopClusterInfo cluster;
    private final DataNodesWindow INSTANCE;
    private Map<UUID, String> hostss;

    public DataNodesWindow(String clusterName) {
        INSTANCE = this;
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
                Agent master = getAgentManager().getAgentByUUID(cluster.getNameNode());

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", master.getUuid().toString());
                map.put(":command", "start");

                TaskUtil.createRequest(Commands.COMMAND_NAME_NODE, "Start Hadoop Cluster", map, INSTANCE, TaskType.START);
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
                Agent master = getAgentManager().getAgentByUUID(cluster.getNameNode());

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", master.getUuid().toString());
                map.put(":command", "stop");

                TaskUtil.createRequest(Commands.COMMAND_NAME_NODE, "Stop Hadoop Cluster", map, INSTANCE, TaskType.STOP);
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
                Agent master = getAgentManager().getAgentByUUID(cluster.getNameNode());

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", master.getUuid().toString());
                map.put(":command", "restart");

                TaskUtil.createRequest(Commands.COMMAND_NAME_NODE, "Restart Hadoop Cluster", map, INSTANCE, TaskType.RESTART);
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
        });

        return addButton;
    }

    private void getStatus() {
        Agent master = getAgentManager().getAgentByUUID(cluster.getNameNode());

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", master.getUuid().toString());

        TaskUtil.createRequest(Commands.STATUS_DATA_NODE, "Get status for Hadoop Data Node", map, INSTANCE, TaskType.STATUS);

        dataNodesTable.refreshDataSource();
    }

    private void configureNode() {
        keys = new ArrayList<String>();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", cluster.getNameNode().toString());
        TaskUtil.createRequest(Commands.COPY_MASTER_KEY, "Configuring new node on Hadoop Cluster", map, INSTANCE, TaskType.CONFIGURE);

        if (!cluster.getNameNode().equals(cluster.getSecondaryNameNode())) {
            map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", cluster.getSecondaryNameNode().toString());
            TaskUtil.createRequest(Commands.COPY_MASTER_KEY, "Configuring new node on Hadoop Cluster", map, INSTANCE, TaskType.CONFIGURE);
        }

        if (!cluster.getJobTracker().equals(cluster.getNameNode()) && !cluster.getJobTracker().equals(cluster.getSecondaryNameNode())) {
            map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", cluster.getJobTracker().toString());
            TaskUtil.createRequest(Commands.COPY_MASTER_KEY, "Configuring new node on Hadoop Cluster", map, INSTANCE, TaskType.CONFIGURE);
        }
    }

    private void addNode() {
        Agent agent = (Agent) agentsComboBox.getValue();

        for (String key : keys) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", agent.getUuid().toString());

            map.put(":PUB_KEY", key);

            TaskUtil.createRequest(Commands.PASTE_MASTER_KEY, "Adding data node to Hadoop Cluster", map, INSTANCE, TaskType.ADD);
        }

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", agent.getUuid().toString());
        TaskUtil.createRequest(Commands.INSTALL_DEB, "Adding data node to Hadoop Cluster", map, INSTANCE, TaskType.ADD);

        map = new HashMap<String, String>();
        map.put(":source", HadoopModule.MODULE_NAME);
        map.put(":uuid", getAgentManager().getAgentByUUID(cluster.getNameNode()).getUuid().toString());
        map.put(":slave-hostname", agent.getHostname());
        TaskUtil.createRequest(Commands.ADD_DATA_NODE, "Adding data node to Hadoop Cluster", map, INSTANCE, TaskType.ADD);

        for (UUID uuid : cluster.getDataNodes()) {
            Agent agentDataNode = getAgentManager().getAgentByUUID(uuid);
            map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", getAgentManager().getAgentByUUID(cluster.getNameNode()).getUuid().toString());
            map.put(":IP", agentDataNode.getHostname());
            TaskUtil.createRequest(Commands.INCLUDE_DATA_NODE, "Adding data node to Hadoop Cluster", map, INSTANCE, TaskType.ADD);
        }

        /*map = new HashMap<String, String>();
         map.put(":source", HadoopModule.MODULE_NAME);
         map.put(":uuid", agent.getUuid().toString());
         //        map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
         createRequest(getCommandManager(), Commands.START_DATA_NODE, addTask, map);*/
    }

    public void readHosts() {
        hostss = new HashMap<UUID, String>();
        for (UUID uuid : getAllNodes()) {
            Agent agent = getAgentManager().getAgentByUUID(uuid);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", agent.getUuid().toString());
            TaskUtil.createRequest(Commands.READ_HOSTNAME, "Read /etc/hosts file", map, INSTANCE, TaskType.READ_HOSTS);
        }
    }

    public void writeHosts(Map<UUID, String> hostss) {

        for (Map.Entry<UUID, String> host : hostss.entrySet()) {
            Agent agent = getAgentManager().getAgentByUUID(host.getKey());
            String hosts = editHosts(host.getValue(), agent);

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", agent.getUuid().toString());
            map.put(":hosts", hosts);
            TaskUtil.createRequest(Commands.WRITE_HOSTNAME, "Write /etc/hosts file", map, INSTANCE, TaskType.WRITE_HOSTS);
        }

        /*HashMap<String, String> map = new HashMap<String, String>();
         map.put(":source", HadoopModule.MODULE_NAME);
         map.put(":uuid", getAgentManager().getAgent(cluster.getNameNode()).getUuid().toString());
         createRequest(getCommandManager(), Commands.REFRESH_DATA_NODES, hadoopWriteHosts, map);*/
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

    public AgentManager getAgentManager() {
        return ServiceLocator.getService(AgentManager.class);
    }

    @Override
    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

        if (task.getData() == TaskType.CONFIGURE && Util.isFinalResponse(response)) {
            keys.add(stdOut);
        } else if (task.getData() == TaskType.READ_HOSTS && Util.isFinalResponse(response)) {
            hostss.put(response.getUuid(), stdOut);
        }

        if (task.isCompleted()) {

            if (task.getData() == TaskType.CONFIGURE) {
                if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                    addNode();
                } else {
                    addButton.setEnabled(true);
                }
            }

            if (task.getData() == TaskType.ADD) {
                if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                    readHosts();
                }
            }

            if (task.getData() == TaskType.STATUS) {
                String status = parseNameNodeStatus(stdOut);
                statusLabel.setValue(status);
                if (status.trim().equalsIgnoreCase("Running")) {
                    disableButtons(1);
                } else {
                    disableButtons(2);
                }
            }

            if (task.getData() == TaskType.START) {
                getStatus();
            }

            if (task.getData() == TaskType.STOP) {
                getStatus();
            }

            if (task.getData() == TaskType.RESTART) {
                getStatus();
            }

            if (task.getData() == TaskType.READ_HOSTS) {
                if (task.getTaskStatus().equals(TaskStatus.SUCCESS)) {
                    writeHosts(hostss);
                }
            }

            if (task.getData() == TaskType.WRITE_HOSTS) {
                if (task.getTaskStatus().equals(TaskStatus.SUCCESS)) {
                    HadoopDAO.saveHadoopClusterInfo(cluster);
                    agentsComboBox.refreshDataSource();
                    dataNodesTable.refreshDataSource();
                } else {
                    agentsComboBox.refreshDataSource();
                }

                addButton.setEnabled(true);
            }
        }

        return null;
    }
}
