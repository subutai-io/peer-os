/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
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
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.ClusterDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.install.InstallOperation;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.TaskType;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.install.UninstallOperation;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 */
public class InstallationStep extends Panel implements ResponseListener {

    private static final Logger LOG = Logger.getLogger(InstallationStep.class.getName());
    private final TextArea outputTxtArea;
    private final TextArea logTextArea;
    private final Button ok;
    private final Button cancel;
    private final Label indicator;
    private Thread operationTimeoutThread;
    private final TaskRunner taskRunner = new TaskRunner();
    private final AgentManager agentManager;
    private final ClusterConfig config;

    public InstallationStep(final Wizard wizard) {
        this.config = wizard.getConfig();
        agentManager = ServiceLocator.getService(AgentManager.class);

        GridLayout content = new GridLayout(20, 3);
        content.setSizeFull();
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.setMargin(true);

        outputTxtArea = new TextArea("Installation output");
        outputTxtArea.setRows(17);
        outputTxtArea.setColumns(60);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea, 0, 0, 18, 0);

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
                                    startInstallation(false);
                                }
                            }
                        });

            }
        });

        indicator = MgmtApplication.createImage("indicator.gif", 50, 50);

        content.addComponent(ok, 0, 1, 0, 1);
        content.addComponent(cancel, 1, 1, 1, 1);
        content.addComponent(indicator, 19, 0, 19, 0);
        content.setComponentAlignment(indicator, Alignment.TOP_RIGHT);

        logTextArea = new TextArea("Command output");
        logTextArea.setRows(17);
        logTextArea.setColumns(60);
        logTextArea.setImmediate(true);
        logTextArea.setWordwrap(true);

        content.addComponent(logTextArea, 0, 2, 18, 2);

        addComponent(content);

    }

    public void startInstallation(final boolean install) {
        try {
            //stop any running installation
            taskRunner.removeAllTaskCallbacks();
            final Operation installOperation = install ? new InstallOperation(config) : new UninstallOperation(config);
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
                                    ClusterDAO.deleteMongoClusterInfo(config.getClusterName());
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
            LOG.log(Level.SEVERE, "Error in startInstallation", e);
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

    @Override
    public void onResponse(Response response) {
        taskRunner.feedResponse(response);
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
