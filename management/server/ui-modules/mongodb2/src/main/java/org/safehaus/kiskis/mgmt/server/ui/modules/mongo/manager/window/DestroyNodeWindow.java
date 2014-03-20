/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.window;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;
import java.text.MessageFormat;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Config;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.MongoDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.NodeType;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Tasks;

/**
 *
 * @author dilshat
 */
public class DestroyNodeWindow extends Window {

    private static final Logger LOG = Logger.getLogger(DestroyNodeWindow.class.getName());

    private final TextArea outputTxtArea;
    private final Button ok;
    private final Label indicator;
    private final Config config;
    private final NodeType nodeType;
    private final Agent agent;

    public DestroyNodeWindow(Config config, NodeType nodeType, Agent agent) {
        super("Destroy node");
        setModal(true);
        setClosable(false);

        this.config = config;
        this.nodeType = nodeType;
        this.agent = agent;

        setWidth(650, DestroyNodeWindow.UNITS_PIXELS);

        GridLayout content = new GridLayout(10, 2);
        content.setSizeFull();
        content.setMargin(true);
        content.setSpacing(true);

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setRows(13);
        outputTxtArea.setColumns(43);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea, 0, 0, 9, 0);
        ok = new Button("Ok");
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //close window   
                MgmtApplication.removeCustomWindow(getWindow());
            }
        });

        indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
        indicator.setVisible(false);

        content.addComponent(ok, 9, 1, 9, 1);
        content.addComponent(indicator, 5, 1, 8, 1);
        content.setComponentAlignment(indicator, Alignment.MIDDLE_RIGHT);
        content.setComponentAlignment(ok, Alignment.MIDDLE_LEFT);

        addComponent(content);
    }

    private void start() {
        MongoModule.getExecutor().execute(new Runnable() {

            public void run() {
                addOutput(String.format("Removing node %s", agent.getHostname()));
                if (nodeType == NodeType.CONFIG_NODE) {
                    config.getConfigServers().remove(agent);
                    //restart routers
                    Task stopMongoTask = MongoModule.getTaskRunner().
                            executeTask(Tasks.getStopMongoTask(config.getRouterServers()));
                    //don't check status of this task since this task always ends with execute_timeouted
                    if (stopMongoTask.isCompleted()) {
                        Task startRoutersTask = MongoModule.getTaskRunner().
                                executeTask(Tasks.getStartRoutersTask2(config.getRouterServers(),
                                                config.getConfigServers(), config));
                        //don't check status of this task since this task always ends with execute_timeouted
                        if (startRoutersTask.isCompleted()) {
                            //check number of started routers
                            int numberOfRoutersRestarted = 0;
                            for (Map.Entry<UUID, Result> res : startRoutersTask.getResults().entrySet()) {
                                if (res.getValue().getStdOut().contains("child process started successfully, parent exiting")) {
                                    numberOfRoutersRestarted++;
                                }
                            }
                            if (numberOfRoutersRestarted != config.getRouterServers().size()) {
                                addOutput("Not all routers restarted. Use Terminal module to restart them");
                            }

                        } else {
                            addOutput("Could not restart routers. Use Terminal module to restart them");
                        }
                    } else {
                        addOutput("Could not restart routers. Use Terminal module to restart them");
                    }

                } else if (nodeType == NodeType.DATA_NODE) {
                    config.getDataNodes().remove(agent);
                    //unregister from primary
                    Task findPrimaryNodeTask = MongoModule.getTaskRunner().
                            executeTask(Tasks.getFindPrimaryNodeTask(agent, config));

                    if (findPrimaryNodeTask.isCompleted()) {
                        Pattern p = Pattern.compile("primary\" : \"(.*)\"");
                        Matcher m = p.matcher(findPrimaryNodeTask.getResults().entrySet().iterator().next().getValue().getStdOut());
                        Agent primaryNodeAgent = null;
                        if (m.find()) {
                            String primaryNodeHost = m.group(1);
                            if (!Util.isStringEmpty(primaryNodeHost)) {
                                String hostname = primaryNodeHost.split(":")[0].replace("." + config.getDomainName(), "");
                                primaryNodeAgent = MongoModule.getAgentManager().getAgentByHostname(hostname);
                            }
                        }
                        if (primaryNodeAgent != null) {
                            if (primaryNodeAgent != agent) {
                                Task unregisterSecondaryNodeFromPrimaryTask
                                        = MongoModule.getTaskRunner().
                                        executeTask(
                                                Tasks.getUnregisterSecondaryFromPrimaryTask(
                                                        primaryNodeAgent, agent, config));
                                if (unregisterSecondaryNodeFromPrimaryTask.getTaskStatus() != TaskStatus.SUCCESS) {
                                    addOutput("Could not unregister this node from replica set, skipping...");
                                }
                            }
                        } else {
                            addOutput("Could not determine primary node for unregistering from replica set, skipping...");
                        }
                    } else {
                        addOutput("Could not determine primary node for unregistering from replica set, skipping...");
                    }

                } else if (nodeType == NodeType.ROUTER_NODE) {
                    config.getRouterServers().remove(agent);
                }
                //destroy lxc
                Agent physicalAgent = MongoModule.getAgentManager().getAgentByHostname(agent.getParentHostName());
                if (physicalAgent == null) {
                    addOutput(
                            String.format("Could not determine physical parent of %s. Use LXC module to cleanup",
                                    agent.getHostname()));
                } else {
                    if (!MongoModule.getLxcManager().destroyLxcOnHost(physicalAgent, agent.getHostname())) {
                        addOutput("Could not destroy lxc container. Use LXC module to cleanup");
                    }
                }
                //update db
                if (!MongoDAO.saveMongoClusterInfo(config)) {
                    addOutput(String.format("Error while updating cluster info [%s] in DB. Check logs",
                            config.getClusterName()));
                }
                addOutput("Done");
                hideProgress();
            }
        });

    }

    public void startOperation() {
        try {
            if (nodeType == NodeType.CONFIG_NODE && config.getConfigServers().size() == 1) {
                addOutput("This is the last configuration server in the cluster. Please, destroy cluster instead");
                return;
            } else if (nodeType == NodeType.DATA_NODE && config.getDataNodes().size() == 1) {
                addOutput("This is the last data node in the cluster. Please, destroy cluster instead");
                return;
            } else if (nodeType == NodeType.ROUTER_NODE && config.getRouterServers().size() == 1) {
                addOutput("This is the last router in the cluster. Please, destroy cluster instead");
                return;
            }
            showProgress();
            start();
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
