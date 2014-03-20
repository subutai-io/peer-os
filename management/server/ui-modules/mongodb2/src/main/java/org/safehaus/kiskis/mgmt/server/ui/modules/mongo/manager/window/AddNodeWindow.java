/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.window;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.NodeType;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.operation.AddRouterOperation;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.operation.AddDataNodeOperation;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Config;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.TaskType;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.MongoDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.taskrunner.Operation;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Constants;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class AddNodeWindow extends Window {

    private static final Logger LOG = Logger.getLogger(AddNodeWindow.class.getName());

    private final TextArea outputTxtArea;
    private final Button ok;
    private final Label indicator;
    private final Config config;

    public AddNodeWindow(final Config config) {
        super("Add New Node");
        setModal(true);
        setClosable(false);

        this.config = config;

        setWidth(600, AddNodeWindow.UNITS_PIXELS);

        GridLayout content = new GridLayout(1, 3);
        content.setSizeFull();
        content.setMargin(true);
        content.setSpacing(true);

        HorizontalLayout topContent = new HorizontalLayout();
        topContent.setSpacing(true);

        content.addComponent(topContent);

        final ComboBox nodeTypeCombo = new ComboBox();
        nodeTypeCombo.setMultiSelect(false);
        nodeTypeCombo.setImmediate(true);
        nodeTypeCombo.setNullSelectionAllowed(false);
        nodeTypeCombo.setTextInputAllowed(false);
        nodeTypeCombo.setWidth(150, Sizeable.UNITS_PIXELS);

        nodeTypeCombo.addItem(NodeType.ROUTER_NODE);
        nodeTypeCombo.setItemCaption(NodeType.ROUTER_NODE, "Add Router");
        nodeTypeCombo.addItem(NodeType.DATA_NODE);
        nodeTypeCombo.setItemCaption(NodeType.DATA_NODE, "Add Data Node");
        nodeTypeCombo.setValue(NodeType.DATA_NODE);
        topContent.addComponent(nodeTypeCombo);

        final Button addNodeBtn = new Button("Add");
        topContent.addComponent(addNodeBtn);

        addNodeBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                NodeType nodeType = (NodeType) nodeTypeCombo.getValue();
                if (nodeType == NodeType.DATA_NODE && config.getDataNodes().size() == 7) {
                    show("Replica set cannot have more than 7 members");
                } else {
                    addNodeBtn.setEnabled(false);
                    start(nodeType);
                }
            }
        });

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setRows(13);
        outputTxtArea.setColumns(43);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea);

        indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
        indicator.setVisible(false);

        ok = new Button("Ok");
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //close window   
                MgmtApplication.removeCustomWindow(getWindow());
            }
        });

        HorizontalLayout bottomContent = new HorizontalLayout();
        bottomContent.addComponent(indicator);
        bottomContent.setComponentAlignment(indicator, Alignment.MIDDLE_RIGHT);
        bottomContent.addComponent(ok);

        content.addComponent(bottomContent);
        content.setComponentAlignment(bottomContent, Alignment.MIDDLE_RIGHT);

        addComponent(content);
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

    private void start(final NodeType nodeType) {
        showProgress();
        MongoModule.getExecutor().execute(new Runnable() {

            public void run() {
                Map<Agent, Integer> bestServers = MongoModule.getLxcManager().getPhysicalServersWithLxcSlots();

                if (bestServers.isEmpty()) {
                    addOutput("No servers available to accommodate new lxc containers");
                    hideProgress();
                    return;
                }
                Agent physicalAgent = bestServers.entrySet().iterator().next().getKey();

                //clone lxc
                StringBuilder lxcHostname;
                if (nodeType == NodeType.DATA_NODE) {
                    lxcHostname = new StringBuilder(physicalAgent.getHostname()).
                            append(Common.PARENT_CHILD_LXC_SEPARATOR).
                            append("mongo-data-").append(Util.generateTimeBasedUUID());
                } else {
                    lxcHostname = new StringBuilder(physicalAgent.getHostname()).
                            append(Common.PARENT_CHILD_LXC_SEPARATOR).
                            append("mongo-rout-").append(Util.generateTimeBasedUUID());
                }
                if (lxcHostname.length() > 64) {
                    lxcHostname.setLength(64);
                }
                boolean result = MongoModule.getLxcManager().cloneLxcOnHost(physicalAgent, lxcHostname.toString());
                if (!result) {
                    addOutput(String.format(
                            "Cloning of lxc container %s failed. Use LXC module to cleanup. Operation aborted",
                            lxcHostname.toString()));
                    hideProgress();
                    return;
                } else {
                    addOutput(String.format(
                            "Successfuly cloned %s lxc container",
                            lxcHostname.toString()));
                }

                //start lxc
                result = MongoModule.getLxcManager().startLxcOnHost(physicalAgent, lxcHostname.toString());
                if (!result) {
                    addOutput(String.format(
                            "Starting of lxc container %s failed. Use LXC module to cleanup. Operation aborted",
                            lxcHostname.toString()));
                    hideProgress();
                    return;
                } else {
                    addOutput(String.format(
                            "Successfuly started %s lxc container",
                            lxcHostname.toString()));
                }
                //wait for the new lxc agent to connect
                Agent lxcAgent = waitLxcAgent(lxcHostname.toString());
                if (lxcAgent == null) {
                    addOutput("Waiting timeout for lxc agent to connect is up. Giving up!. Use LXC module to cleanup");
                    hideProgress();
                    return;
                }
                //install node
                startOperation(nodeType, lxcAgent);
            }
        });

    }

    private Agent waitLxcAgent(String lxcHostname) {
        long waitStart = System.currentTimeMillis();
        while (!Thread.interrupted()) {
            Agent lxcAgent = MongoModule.getAgentManager().getAgentByHostname(lxcHostname);
            if (lxcAgent != null) {
                return lxcAgent;
            }
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
        return null;
    }

    private void startOperation(final NodeType nodeType, final Agent agent) {
        try {
            final Operation operation
                    = (nodeType == NodeType.DATA_NODE)
                    ? new AddDataNodeOperation(config, agent)
                    : new AddRouterOperation(config, agent);

            addOutput(String.format("Running task %s", operation.peekNextTask().getDescription()));

            MongoModule.getTaskRunner().executeTask(operation.getNextTask(), new TaskCallback() {

                private final StringBuilder routersOutput = new StringBuilder();

                @Override
                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    if (task.getData() == TaskType.FIND_PRIMARY_NODE) {

                        if (task.isCompleted()) {
                            Agent primaryNodeAgent = null;
                            Pattern p = Pattern.compile("primary\" : \"(.*)\"");
                            Matcher m = p.matcher(stdOut);
                            if (m.find()) {
                                String primaryNodeHost = m.group(1);
                                if (!Util.isStringEmpty(primaryNodeHost)) {
                                    String hostname = primaryNodeHost.split(":")[0].replace("." + config.getDomainName(), "");
                                    primaryNodeAgent = MongoModule.getAgentManager().getAgentByHostname(hostname);
                                }
                            }

                            if (primaryNodeAgent != null) {
                                Request registerSecondaryWithPrimaryCmd = operation.peekNextTask().getRequests().iterator().next();
                                registerSecondaryWithPrimaryCmd.setUuid(primaryNodeAgent.getUuid());
                            } else {
                                task.setTaskStatus(TaskStatus.FAIL);
                            }
                        }
                    } else if (task.getData() == TaskType.START_REPLICA_SET
                            || task.getData() == TaskType.START_ROUTERS
                            || task.getData() == TaskType.START_CONFIG_SERVERS
                            || task.getData() == TaskType.RESTART_ROUTERS) {
                        if (task.getData() == TaskType.RESTART_ROUTERS && !Util.isStringEmpty(response.getStdOut())) {
                            routersOutput.append(response.getStdOut());
                        }

                        if ((task.getData() == TaskType.RESTART_ROUTERS
                                && Util.countNumberOfOccurences(routersOutput.toString(),
                                        "child process started successfully, parent exiting")
                                == config.getRouterServers().size())
                                || (task.getData() != TaskType.RESTART_ROUTERS
                                && stdOut.indexOf(
                                        "child process started successfully, parent exiting") > -1)) {
                            task.setTaskStatus(TaskStatus.SUCCESS);
                            task.setCompleted(true);
                            MongoModule.getTaskRunner().removeTaskCallback(task.getUuid());
                        }
                    }

                    if (task.isCompleted()) {
                        if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                            addOutput(String.format("Task %s succeeded", task.getDescription()));

                            if (operation.hasNextTask()) {
                                addOutput(String.format("Running task %s", operation.peekNextTask().getDescription()));

                                return operation.getNextTask();
                            } else {
                                addOutput(String.format("Operation %s completed", operation.getDescription()));
                                hideProgress();
                                if (nodeType == NodeType.DATA_NODE) {
                                    config.getDataNodes().add(agent);
                                    MongoDAO.saveMongoClusterInfo(config);
                                } else if (nodeType == NodeType.CONFIG_NODE) {
                                    config.getConfigServers().add(agent);
                                    MongoDAO.saveMongoClusterInfo(config);
                                } else if (nodeType == NodeType.ROUTER_NODE) {
                                    config.getRouterServers().add(agent);
                                    MongoDAO.saveMongoClusterInfo(config);
                                }
                            }
                        } else {
                            addOutput(String.format("Task %s failed", task.getDescription()));
                            addOutput(String.format("Operation %s failed", operation.getDescription()));
                            hideProgress();
                        }
                    }

                    return null;
                }
            });
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in startOperation", e);
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

    private void addOutput(String output) {
        if (!Util.isStringEmpty(output)) {
            outputTxtArea.setValue(
                    MessageFormat.format("{0}\n\n{1}",
                            outputTxtArea.getValue(),
                            output));
            outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
        }
    }

}
