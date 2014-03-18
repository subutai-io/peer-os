/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.operation.InstallClusterOperation;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.TaskType;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
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
    private final Button done;
    private final Button back;
    private final Label indicator;
    private Thread operationTimeoutThread;

    private final ClusterConfig config;
    private final AgentManager agentManager;
    private final TaskRunner taskRunner;
    private final LxcManager lxcManager;

    public InstallationStep(final Wizard wizard) {

        this.config = wizard.getConfig();
        this.taskRunner = MongoModule.getTaskRunner();
        this.agentManager = MongoModule.getAgentManager();
        this.lxcManager = MongoModule.getLxcManager();

        setSizeFull();

        GridLayout grid = new GridLayout(20, 10);
        grid.setSizeFull();
        grid.setMargin(true);

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setSizeFull();
        outputTxtArea.setRows(13);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        grid.addComponent(outputTxtArea, 0, 0, 18, 3);

        done = new Button("Done");
        done.setEnabled(false);
        done.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.init();
            }
        });
        back = new Button("Back");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });

        indicator = MgmtApplication.createImage("indicator.gif", 50, 11);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(back);
        buttons.addComponent(done);
        grid.addComponent(buttons, 0, 9, 5, 9);
        grid.addComponent(indicator, 19, 0, 19, 0);

        logTextArea = new TextArea("Node output");
        logTextArea.setSizeFull();
        logTextArea.setRows(13);
        logTextArea.setImmediate(true);
        logTextArea.setWordwrap(true);

        grid.addComponent(logTextArea, 0, 4, 18, 8);

        addComponent(grid);

    }

    public void startOperation() {
        try {
            showProgress();

            //perform lxc container installation and bootstrap here
            Map<Agent, Integer> bestServers = lxcManager.getPhysicalServersWithLxcSlots();

            if (bestServers.isEmpty()) {
                addOutput("No servers available to accommodate new lxc containers");
                hideProgress();
                return;
            }

            //check number if available lxc slots
            int numberOfLxcsNeeded = config.getNumberOfConfigServers() + config.getNumberOfDataNodes() + config.getNumberOfRouters();

            int numOfAvailableLxcSlots = 0;
            for (Map.Entry<Agent, Integer> srv : bestServers.entrySet()) {
                numOfAvailableLxcSlots += srv.getValue();
            }

            if (numOfAvailableLxcSlots < numberOfLxcsNeeded) {
                addOutput(String.format("Only %s lxc containers can be created", numOfAvailableLxcSlots));
                hideProgress();
                return;
            }

            //clone lxc containers
            clone(bestServers);
            install();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in startOperation", e);
        }

    }

    private void clone(Map<Agent, Integer> bestServers) {

    }

    private void install() {
        try {

            final Operation installOperation = new InstallClusterOperation(config);
            runTimeoutThread(installOperation);
            addOutput(String.format("Operation %s started", installOperation.getDescription()));
            addOutput(String.format("Running task %s", installOperation.peekNextTask().getDescription()));
            addLog(String.format("======= %s =======", installOperation.peekNextTask().getDescription()));

            taskRunner.executeTask(installOperation.getNextTask(), new TaskCallback() {
                private final StringBuilder startConfigServersOutput = new StringBuilder();
                private final StringBuilder startRoutersOutput = new StringBuilder();
                private final StringBuilder startDataNodesOutput = new StringBuilder();

                @Override
                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

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
                                return installOperation.getNextTask();
                            } else {
                                installOperation.setCompleted(true);
                                addOutput(String.format("Operation %s completed", installOperation.getDescription()));
                                hideProgress();
                            }
                        } else {
                            installOperation.setCompleted(true);
                            addOutput(String.format("Task %s failed", task.getDescription()));
                            addOutput(String.format("Operation %s failed", installOperation.getDescription()));
                            hideProgress();
                        }
                    }

                    return null;
                }
            });
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in install", e);
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
        done.setEnabled(false);
        back.setEnabled(false);
    }

    private void hideProgress() {
        indicator.setVisible(false);
        done.setEnabled(true);
        back.setEnabled(true);
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
