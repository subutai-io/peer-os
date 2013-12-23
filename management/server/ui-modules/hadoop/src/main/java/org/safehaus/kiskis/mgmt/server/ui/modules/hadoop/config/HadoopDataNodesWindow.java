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

import java.util.List;

public final class HadoopDataNodesWindow extends Window {

    private Button startButton, stopButton, restartButton;
    private Label statusLabel;
    private AgentsComboBox agentsComboBox;
    private DataNodesTable dataNodesTable;

    private HadoopClusterInfo cluster;
    private Task addTask;

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
        final String addNode = "{\n" +
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

        Button button = new Button("Add");
        button.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                Agent agent = (Agent) agentsComboBox.getValue();
                cluster.getDataNodes().add(agent.getUuid());

                addTask = createTask("Adding data node to Hadoop Cluster");
                createRequest(addNode, addTask, getAgentManager().getAgent(cluster.getNameNode()), agent);
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
        if (addTask != null) {
            Task task = getCommandManager().getTask(response.getTaskUuid());
            List<ParseResult> list = getCommandManager().parseTask(task, true);
            task = getCommandManager().getTask(response.getTaskUuid());

            if (!list.isEmpty()) {
                if (task.equals(addTask)) {
                    if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                        agentsComboBox.refreshDataSource();
                        dataNodesTable.refreshDataSource();
                        getCommandManager().saveHadoopClusterData(cluster);
                        statusLabel.setValue("Data Node added");
                    } else {
                        cluster = getCommandManager().getHadoopClusterData(cluster.getClusterName());
                        agentsComboBox.refreshDataSource();
                    }
                    addTask = null;
                }
            }
        }

        dataNodesTable.onCommand(response);
    }

    private Task createTask(String description) {
        Task clusterTask = new Task();
        clusterTask.setTaskStatus(TaskStatus.NEW);
        clusterTask.setDescription(description);
        getCommandManager().saveTask(clusterTask);

        return clusterTask;
    }

    private Request createRequest(final String command, Task task, Agent agent, Agent slave) {
        String json = command;
        json = json.replaceAll(":taskUuid", task.getUuid().toString());
        json = json.replaceAll(":source", HadoopModule.MODULE_NAME);

        json = json.replaceAll(":uuid", agent.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", task.getIncrementedReqSeqNumber().toString());

        if (slave != null) {
            json = json.replaceAll(":slave-hostname", slave.getHostname());
        }


        Request request = CommandJson.getRequest(json);
        if (getCommandManager() != null) {
            getCommandManager().executeCommand(new Command(request));
        }

        return request;
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
