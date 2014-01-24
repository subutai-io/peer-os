/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.ConfirmationDialogCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.MongoDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.operation.InstallClusterOperation;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.TaskType;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.operation.UninstallClusterOperation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
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
public class InstallationStep extends Panel {

    private static final Logger LOG = Logger.getLogger(InstallationStep.class.getName());
    private final TextArea outputTxtArea;
    private final TextArea logTextArea;
    private final Button ok;
    private final Button cancel;
    private final Label indicator;
    private Thread operationTimeoutThread;

    private final AgentManager agentManager;
    private final ClusterConfig config;
    private final TaskRunner taskRunner;

    public InstallationStep(final Wizard wizard) {

        this.config = wizard.getConfig();
        this.taskRunner = wizard.getTaskRunner();
        agentManager = ServiceLocator.getService(AgentManager.class);

        setSizeFull();

        GridLayout grid = new GridLayout(20, 10);
        grid.setSizeFull();
        grid.setSpacing(true);
        grid.setMargin(true);

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setSizeFull();
        outputTxtArea.setRows(10);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        grid.addComponent(outputTxtArea, 0, 0, 18, 3);

        ok = new Button("Ok");
        ok.setEnabled(false);
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.init();
            }
        });
        cancel = new Button("Cancel");
        cancel.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                MgmtApplication.showConfirmationDialog(
                        "Undo cluster installation",
                        "Do you want to revert cluster installation?\nWarning: If 'Install mongo' task is currently running,\ndpkg process might be locked and uninstallation would fail", "Yes", "No", new ConfirmationDialogCallback() {

                            @Override
                            public void response(boolean ok) {
                                if (ok) {
                                    cancel.setEnabled(false);
                                    startOperation(false);
                                }
                            }
                        });

            }
        });

        indicator = MgmtApplication.createImage("indicator.gif", 50, 50);

        grid.addComponent(ok, 0, 4, 0, 4);
        grid.addComponent(cancel, 1, 4, 1, 4);
        grid.addComponent(indicator, 19, 0, 19, 0);

        logTextArea = new TextArea("Node output");
        logTextArea.setSizeFull();
        logTextArea.setRows(10);
        logTextArea.setImmediate(true);
        logTextArea.setWordwrap(true);

        grid.addComponent(logTextArea, 0, 5, 18, 9);

        addComponent(grid);

    }

    public void startOperation(final boolean install) {
        try {
            //stop any running installation
            taskRunner.removeAllTaskCallbacks();
            final Operation installOperation = install ? new InstallClusterOperation(config) : new UninstallClusterOperation(config);
            runTimeoutThread(installOperation);
            showProgress();
            addOutput(String.format("Operation %s started", installOperation.getDescription()));
            addOutput(String.format("Running task %s", installOperation.peekNextTask().getDescription()));
            addLog(String.format("======= %s =======", installOperation.peekNextTask().getDescription()));

            taskRunner.runTask(installOperation.getNextTask(), new TaskCallback() {
                private final StringBuilder startConfigServersOutput = new StringBuilder();
                private final StringBuilder startRoutersOutput = new StringBuilder();
                private final StringBuilder startDataNodesOutput = new StringBuilder();

                @Override
                public void onResponse(Task task, Response response) {

                    if (task.getData() != null) {
                        boolean taskOk = false;
                        if (task.getData() == TaskType.START_CONFIG_SERVERS) {
                            startConfigServersOutput.append(response.getStdOut());
                            if (Util.countNumberOfOccurences(startConfigServersOutput.toString(),
                                    "child process started successfully, parent exiting")
                                    == config.getConfigServers().size()) {
                                taskOk = true;
                            }
                        } else if (task.getData() == TaskType.START_ROUTERS) {
                            startRoutersOutput.append(response.getStdOut());
                            if (Util.countNumberOfOccurences(startRoutersOutput.toString(),
                                    "child process started successfully, parent exiting")
                                    == config.getRouterServers().size()) {
                                taskOk = true;
                            }
                        } else if (task.getData() == TaskType.START_REPLICA_SET) {
                            startDataNodesOutput.append(response.getStdOut());
                            if (Util.countNumberOfOccurences(startDataNodesOutput.toString(),
                                    "child process started successfully, parent exiting")
                                    == config.getDataNodes().size()) {
                                taskOk = true;
                            }
                        }
                        if (taskOk) {
                            task.setCompleted(true);
                            task.setTaskStatus(TaskStatus.SUCCESS);
                            taskRunner.removeTaskCallback(task.getUuid());
                        }
                    }

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
                                if (!install) {
                                    MongoDAO.deleteMongoClusterInfo(config.getClusterName());
                                }
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
        cancel.setEnabled(true);
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
