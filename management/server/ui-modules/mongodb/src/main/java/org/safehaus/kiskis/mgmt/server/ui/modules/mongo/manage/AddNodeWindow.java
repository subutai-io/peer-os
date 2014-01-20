/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;
import java.text.MessageFormat;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.MongoDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.install.UninstallOperation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 */
public class AddNodeWindow extends Window {

    private static final Logger LOG = Logger.getLogger(AddNodeWindow.class.getName());

    private final TextArea outputTxtArea;
    private final TextArea logTextArea;
    private final Button ok;
    private final Label indicator;
    private final TaskRunner taskRunner;
    private final AgentManager agentManager;
    private final ClusterConfig config;
    private Thread operationTimeoutThread;
    private boolean succeeded = false;

    public AddNodeWindow(ClusterConfig config, TaskRunner taskRunner) {
        super("Add New Node");
        setModal(true);

        this.taskRunner = taskRunner;
        this.config = config;
        agentManager = ServiceLocator.getService(AgentManager.class);

        setWidth(650, AddNodeWindow.UNITS_PIXELS);

        GridLayout content = new GridLayout(20, 4);
        content.setSizeFull();
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.setMargin(true);
        content.setSpacing(true);

        HorizontalLayout topContent = new HorizontalLayout();
        topContent.setSpacing(true);

        content.addComponent(topContent, 0, 0, 18, 0);

        Label label = new Label("Select node:");

        topContent.addComponent(label);

        final ComboBox nodesCombo = new ComboBox();
        nodesCombo.setMultiSelect(false);
        nodesCombo.setImmediate(true);
        nodesCombo.setTextInputAllowed(false);
        nodesCombo.setWidth(200, Sizeable.UNITS_PIXELS);

        Set<Agent> agents = agentManager.getLxcAgents();

        agents.removeAll(config.getConfigServers());
        agents.removeAll(config.getRouterServers());
        agents.removeAll(config.getDataNodes());

        if (agents.size() > 0) {
            for (Agent agent : agents) {
                nodesCombo.addItem(agent);
                nodesCombo.setItemCaption(agent, agent.getHostname());
            }
            nodesCombo.setValue(agents.iterator().next());
        }

        topContent.addComponent(nodesCombo);

        final ComboBox nodeTypeCombo = new ComboBox();
        nodeTypeCombo.setMultiSelect(false);
        nodeTypeCombo.setImmediate(true);
        nodeTypeCombo.setTextInputAllowed(false);
        nodeTypeCombo.setWidth(200, Sizeable.UNITS_PIXELS);

        nodeTypeCombo.addItem(NodeType.CONFIG_NODE);
        nodeTypeCombo.setItemCaption(NodeType.CONFIG_NODE, "Add as Config Server");
        nodeTypeCombo.addItem(NodeType.ROUTER_NODE);
        nodeTypeCombo.setItemCaption(NodeType.ROUTER_NODE, "Add as Router Server");
        nodeTypeCombo.addItem(NodeType.DATA_NODE);
        nodeTypeCombo.setItemCaption(NodeType.DATA_NODE, "Add as Data Node");
        nodeTypeCombo.setValue(NodeType.CONFIG_NODE);
        topContent.addComponent(nodeTypeCombo);

        Button addNodeBtn = new Button("Add");
        topContent.addComponent(addNodeBtn);

        addNodeBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                NodeType nodeType = (NodeType) nodeTypeCombo.getValue();
                Agent agent = (Agent) nodesCombo.getValue();
                if (agent == null) {
                    show("Please, select node");
                } else if (nodeType == null) {
                    show("Please, select node type");
                } else {
                    show("OK");
                }
            }
        });

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setRows(17);
        outputTxtArea.setColumns(45);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea, 0, 1, 18, 1);

        indicator = MgmtApplication.createImage("indicator.gif", 50, 50);
        indicator.setVisible(false);

        content.addComponent(indicator, 19, 1, 19, 1);
        content.setComponentAlignment(indicator, Alignment.TOP_RIGHT);

        logTextArea = new TextArea("Node output");
        logTextArea.setRows(17);
        logTextArea.setColumns(45);
        logTextArea.setImmediate(true);
        logTextArea.setWordwrap(true);

        content.addComponent(logTextArea, 0, 2, 18, 2);

        ok = new Button("Ok");
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //close window   
                MgmtApplication.removeCustomWindow(getWindow());
            }
        });

        content.addComponent(ok, 18, 3, 18, 3);

        addComponent(content);
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

    public void startOperation() {
        try {
            //stop any running installation
            taskRunner.removeAllTaskCallbacks();
            final Operation installOperation = new UninstallOperation(config);
            runTimeoutThread(installOperation);
            showProgress();
            addOutput(String.format("Operation %s started", installOperation.getDescription()));
            addOutput(String.format("Running task %s", installOperation.peekNextTask().getDescription()));
            addLog(String.format("======= %s =======", installOperation.peekNextTask().getDescription()));

            taskRunner.runTask(installOperation.getNextTask(), new TaskCallback() {

                @Override
                public void onResponse(Task task, Response response) {

                    Agent agent = agentManager.getAgentByUUID(response.getUuid());
                    addLog(String.format("%s:\n%s\n%s",
                            agent != null
                            ? agent.getHostname() : String.format("Offline[%s]", response.getUuid()),
                            Util.isStringEmpty(response.getStdOut()) ? "" : response.getStdOut(),
                            Util.isStringEmpty(response.getStdErr()) ? "" : response.getStdErr()));

                    if (Util.isFinalResponse(response)) {
                        if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                            addLog(String.format("Exit code: %d", response.getExitCode()));
                        } else {
                            addLog("Command timed out");
                        }
                    }

                    if (task.isCompleted()) {
                        if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                            addOutput(String.format("Task %s succeeded", task.getDescription()));
                            if (installOperation.hasNextTask()) {
                                addOutput(String.format("Running task %s", installOperation.peekNextTask().getDescription()));
                                addLog(String.format("======= %s =======", installOperation.peekNextTask().getDescription()));
                                taskRunner.runTask(installOperation.getNextTask(), this);
                            } else {
                                installOperation.setCompleted(true);
                                addOutput(String.format("Operation %s completed", installOperation.getDescription()));
                                hideProgress();
                                MongoDAO.deleteMongoClusterInfo(config.getClusterName());
                                succeeded = true;
                            }
                        } else {
                            installOperation.setCompleted(true);
                            addOutput(String.format("Task %s failed", task.getDescription()));
                            addOutput(String.format("Operation %s failed", installOperation.getDescription()));
                            hideProgress();
                        }
                    }
                }
            });
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in startOperation", e);
        }
    }

    private void runTimeoutThread(final Operation operation) {
        try {
            if (operationTimeoutThread != null && operationTimeoutThread.isAlive()) {
                operationTimeoutThread.interrupt();
            }
            operationTimeoutThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //wait for timeout + 5 sec just in case
                        Thread.sleep(operation.getTotalTimeout() * 1000 + 5000);
                        if (!operation.isCompleted()) {
                            addOutput(String.format(
                                    "Operation %s timed out!!!",
                                    operation.getDescription()));
                            hideProgress();
                        }
                    } catch (InterruptedException ex) {
                    }
                }
            });
            operationTimeoutThread.start();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in runTimeoutThread", e);
        }
    }

    private void showProgress() {
        indicator.setVisible(true);
        ok.setEnabled(false);
    }

    private void hideProgress() {
        indicator.setVisible(false);
        ok.setEnabled(true);
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    private void addOutput(String output) {
        if (!Util.isStringEmpty(output)) {
            outputTxtArea.setValue(
                    MessageFormat.format("{0}\n\n{1}",
                            outputTxtArea.getValue(),
                            output));
            outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
        }
    }

    private void addLog(String log) {
        if (!Util.isStringEmpty(log)) {
            logTextArea.setValue(
                    MessageFormat.format("{0}\n\n{1}",
                            logTextArea.getValue(),
                            log));
            logTextArea.setCursorPosition(logTextArea.getValue().toString().length() - 1);
        }
    }

}
