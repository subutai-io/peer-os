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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.operation.InstallClusterOperation;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Operation;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.TaskType;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.NodeType;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.MongoDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Constants;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 * @todo remove unnecessary commands like uninstall prev mongo
 * @todo place properly process indicator here and in destroyNodeWindow (look to
 * destroyClusterWindow)
 * @todo show all config fields in manager UI
 * @todo all empty execute_response messages from agents shud be indicated as
 * RUNNING
 * @todo add window for destroy node operation
 *
 */
public class InstallationStep extends Panel {

    private static final Logger LOG = Logger.getLogger(InstallationStep.class.getName());
    private final TextArea outputTxtArea;
    private final TextArea logTextArea;
    private final Button done;
    private final Button back;
    private final Label indicator;
    private final Config config;

    public InstallationStep(final Wizard wizard) {

        this.config = wizard.getConfig();

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
            Map<Agent, Integer> bestServers = MongoModule.getLxcManager().getPhysicalServersWithLxcSlots();

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

            start(bestServers);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in startOperation", e);
        }

    }

    private class CloneInfo {

        private final Agent physicalAgent;
        private final String lxcHostname;
        private final NodeType nodeType;
        private boolean result;

        public CloneInfo(Agent physicalAgent, String lxcHostname, NodeType nodeType) {
            this.physicalAgent = physicalAgent;
            this.lxcHostname = lxcHostname;
            this.nodeType = nodeType;
        }

        public boolean isResult() {
            return result;
        }

        public void setResult(boolean result) {
            this.result = result;
        }

        public Agent getPhysicalAgent() {
            return physicalAgent;
        }

        public String getLxcHostname() {
            return lxcHostname;
        }

        public NodeType getNodeType() {
            return nodeType;
        }

    }

    private class Cloner implements Callable<CloneInfo> {

        private final CloneInfo cloneInfo;

        public Cloner(CloneInfo cloneInfo) {
            this.cloneInfo = cloneInfo;
        }

        public CloneInfo call() throws Exception {
            cloneInfo.setResult(MongoModule.getLxcManager().cloneLxcOnHost(cloneInfo.physicalAgent, cloneInfo.getLxcHostname()));
            return cloneInfo;
        }
    }

    private class Starter implements Callable<CloneInfo> {

        private final CloneInfo cloneInfo;

        public Starter(CloneInfo cloneInfo) {
            this.cloneInfo = cloneInfo;
        }

        public CloneInfo call() throws Exception {
            cloneInfo.setResult(MongoModule.getLxcManager().startLxcOnHost(cloneInfo.physicalAgent, cloneInfo.getLxcHostname()));
            return cloneInfo;
        }
    }

    private void start(final Map<Agent, Integer> bestServers) {
        MongoModule.getExecutor().execute(new Runnable() {

            public void run() {
                List<CloneInfo> cloneInfoList = new ArrayList<CloneInfo>();
                //clone lxc containers
                if (cloneLxcs(bestServers, cloneInfoList)) {
                    addOutput("Lxc containers cloned successfully");
                    //start lxc containers
                    if (startLxcs(cloneInfoList)) {
                        addOutput("Lxc containers started successfully");

                        //wait until all lxc agents connect
                        if (waitAllLxcAgents(cloneInfoList)) {

                            //install mongo
                            Set<Agent> cfgServers = new HashSet<Agent>();
                            Set<Agent> routers = new HashSet<Agent>();
                            Set<Agent> dataNodes = new HashSet<Agent>();
                            for (CloneInfo cloneInfo : cloneInfoList) {
                                if (cloneInfo.getNodeType() == NodeType.CONFIG_NODE) {
                                    cfgServers.add(MongoModule.getAgentManager().getAgentByHostname(cloneInfo.getLxcHostname()));
                                } else if (cloneInfo.getNodeType() == NodeType.ROUTER_NODE) {
                                    routers.add(MongoModule.getAgentManager().getAgentByHostname(cloneInfo.getLxcHostname()));
                                } else if (cloneInfo.getNodeType() == NodeType.DATA_NODE) {
                                    dataNodes.add(MongoModule.getAgentManager().getAgentByHostname(cloneInfo.getLxcHostname()));
                                }
                            }
                            config.setConfigServers(cfgServers);
                            config.setDataNodes(dataNodes);
                            config.setRouterServers(routers);

                            if (persistConfigurationToDb()) {
                                installMongoCluster();
                            } else {
                                addOutput("Could not save new cluster configuration to DB! Please see logs. Use LXC module to cleanup");
                                hideProgress();
                            }
                        } else {
                            addOutput("Waiting timeout for lxc agents to connect is up. Giving up!. Use LXC module to cleanup");
                            hideProgress();
                        }

                    } else {
                        addOutput("Starting of lxc containers failed. Use LXC module to cleanup");
                        hideProgress();
                    }
                } else {
                    addOutput("Cloning of lxc containers failed. Use LXC module to cleanup");
                    hideProgress();
                }
            }
        });

    }

    private boolean waitAllLxcAgents(List<CloneInfo> cloneInfoList) {
        long waitStart = System.currentTimeMillis();
        while (!Thread.interrupted()) {
            boolean allConnected = true;
            for (CloneInfo cloneInfo : cloneInfoList) {
                if (MongoModule.getAgentManager().getAgentByHostname(cloneInfo.getLxcHostname()) == null) {
                    allConnected = false;
                    break;
                }
            }
            if (allConnected) {
                return true;
            } else {
                if (System.currentTimeMillis() - waitStart > Constants.LXC_AGENT_WAIT_TIMEOUT_SEC * 1000) {
                    break;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        }
        return false;
    }

    private boolean persistConfigurationToDb() {
        return MongoDAO.saveMongoClusterInfo(config);
    }

    private boolean startLxcs(List<CloneInfo> cloneInfoList) {
        if (!cloneInfoList.isEmpty()) {
            CompletionService<CloneInfo> completer = new ExecutorCompletionService<CloneInfo>(MongoModule.getExecutor());
            try {
                for (CloneInfo cloneInfo : cloneInfoList) {
                    addOutput(String.format("Starting lxc %s", cloneInfo.lxcHostname));
                    cloneInfo.setResult(false);
                    completer.submit(new Starter(cloneInfo));
                }

                boolean result = true;
                for (int i = 0; i < cloneInfoList.size(); i++) {
                    Future<CloneInfo> future = completer.take();
                    CloneInfo cloneInfo = future.get();
                    result &= cloneInfo.isResult();
                }

                return result;
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }
        return false;
    }

    private boolean cloneLxcs(final Map<Agent, Integer> bestServers, final List<CloneInfo> cloneInfoList) {

        Set<String> configSrvsHostnames = new HashSet<String>();
        Set<String> routersHostnames = new HashSet<String>();
        Set<String> dataNodesHostnames = new HashSet<String>();

        int numOfLxcs = 0;
        for (int i = 1; i <= config.getNumberOfConfigServers(); i++) {
            numOfLxcs++;
            StringBuilder lxcHostname = new StringBuilder("mongo-cfg-").append(Util.generateTimeBasedUUID());
            if (lxcHostname.length() > 64) {
                lxcHostname.setLength(64);
            }
            configSrvsHostnames.add(lxcHostname.toString());
        }
        for (int i = 1; i <= config.getNumberOfRouters(); i++) {
            numOfLxcs++;
            StringBuilder lxcHostname = new StringBuilder("mongo-rout-").append(Util.generateTimeBasedUUID());
            if (lxcHostname.length() > 64) {
                lxcHostname.setLength(64);
            }
            routersHostnames.add(lxcHostname.toString());
        }
        for (int i = 1; i <= config.getNumberOfDataNodes(); i++) {
            numOfLxcs++;
            StringBuilder lxcHostname = new StringBuilder("mongo-data-").append(Util.generateTimeBasedUUID());
            if (lxcHostname.length() > 64) {
                lxcHostname.setLength(64);
            }
            dataNodesHostnames.add(lxcHostname.toString());
        }

        Iterator<String> configSrvsHostnamesIterator = configSrvsHostnames.iterator();
        Iterator<String> routersHostnamesIterator = routersHostnames.iterator();
        Iterator<String> dataNodesHostnamesIterator = dataNodesHostnames.iterator();

        Map<Agent, Integer> sortedBestServers = Util.sortMapByValueDesc(bestServers);

        CompletionService<CloneInfo> completer = new ExecutorCompletionService<CloneInfo>(MongoModule.getExecutor());

        try {
            outerloop:
            for (final Map.Entry<Agent, Integer> entry : sortedBestServers.entrySet()) {
                for (int i = 1; i <= entry.getValue(); i++) {
                    if (configSrvsHostnamesIterator.hasNext()) {
                        final String lxcHostname = new StringBuilder(entry.getKey().getHostname())
                                .append(Common.PARENT_CHILD_LXC_SEPARATOR)
                                .append(configSrvsHostnamesIterator.next()).toString();
                        addOutput(String.format("Cloning lxc %s", lxcHostname));
                        completer.submit(new Cloner(new CloneInfo(entry.getKey(), lxcHostname, NodeType.CONFIG_NODE)));
                    } else if (routersHostnamesIterator.hasNext()) {
                        final String lxcHostname = new StringBuilder(entry.getKey().getHostname())
                                .append(Common.PARENT_CHILD_LXC_SEPARATOR)
                                .append(routersHostnamesIterator.next()).toString();
                        addOutput(String.format("Cloning lxc %s", lxcHostname));
                        completer.submit(new Cloner(new CloneInfo(entry.getKey(), lxcHostname, NodeType.ROUTER_NODE)));
                    } else if (dataNodesHostnamesIterator.hasNext()) {
                        final String lxcHostname = new StringBuilder(entry.getKey().getHostname())
                                .append(Common.PARENT_CHILD_LXC_SEPARATOR)
                                .append(dataNodesHostnamesIterator.next()).toString();
                        addOutput(String.format("Cloning lxc %s", lxcHostname));
                        completer.submit(new Cloner(new CloneInfo(entry.getKey(), lxcHostname, NodeType.DATA_NODE)));
                    } else {
                        break outerloop;
                    }
                }
            }
            boolean result = true;
            for (int i = 0; i < numOfLxcs; i++) {
                Future<CloneInfo> future = completer.take();
                CloneInfo cloneInfo = future.get();
                cloneInfoList.add(cloneInfo);
                result &= cloneInfo.isResult();
            }

            return result;

        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }

        return false;

    }

    private void installMongoCluster() {
        try {

            final Operation installOperation = new InstallClusterOperation(config);
            addOutput(String.format("Operation %s started", installOperation.getDescription()));
            addOutput(String.format("Running task %s", installOperation.peekNextTask().getDescription()));
            addLog(String.format("======= %s =======", installOperation.peekNextTask().getDescription()));

            MongoModule.getTaskRunner().executeTask(installOperation.getNextTask(), new TaskCallback() {
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
                            MongoModule.getTaskRunner().removeTaskCallback(task.getUuid());
                        }
                    }

                    Agent agent = MongoModule.getAgentManager().getAgentByUUID(response.getUuid());
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
                                addOutput(String.format("Operation %s completed", installOperation.getDescription()));
                                hideProgress();
                            }
                        } else {
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
