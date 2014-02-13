/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.callback;

import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Constants;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.TaskType;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.MongoDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.entity.MongoClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AsyncTaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ChainedTaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 */
public class DestroyDataNodeCallback implements ChainedTaskCallback {

    private final Window parentWindow;
    private final AgentManager agentManager;
    private final MongoClusterInfo clusterInfo;
    private final ClusterConfig config;
    private final Agent nodeAgent;
    private final Table dataNodesTable;
    private final Object rowId;
    private final Operation op;
    private final Button checkButton;
    private final Button destroyButton;
    private final Embedded progressIcon;

    public DestroyDataNodeCallback(Window parentWindow, AgentManager agentManager, MongoClusterInfo clusterInfo, ClusterConfig config, Agent nodeAgent, Table dataNodesTable, Object rowId, Operation op, Embedded progressIcon, Button checkButton, Button startButton, Button stopButton, Button destroyButton) {
        this.parentWindow = parentWindow;
        this.agentManager = agentManager;
        this.clusterInfo = clusterInfo;
        this.config = config;
        this.nodeAgent = nodeAgent;
        this.dataNodesTable = dataNodesTable;
        this.rowId = rowId;
        this.op = op;
        this.progressIcon = progressIcon;
        this.checkButton = checkButton;
        this.destroyButton = destroyButton;
        progressIcon.setVisible(true);
        checkButton.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        destroyButton.setEnabled(false);
    }

    @Override
    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
        if (task.getData() == TaskType.FIND_PRIMARY_NODE) {
            if (task.isCompleted()) {
                Pattern p = Pattern.compile("primary\" : \"(.*)\"");
                Matcher m = p.matcher(stdOut.toString());
                Agent primaryNodeAgent = null;
                if (m.find()) {
                    String primaryNodeHost = m.group(1);
                    if (!Util.isStringEmpty(primaryNodeHost)) {
                        String hostname = primaryNodeHost.split(":")[0].replace(Constants.DOMAIN, "");
                        primaryNodeAgent = agentManager.getAgentByHostname(hostname);
                    }
                }

//                if (primaryNodeAgent == null) {
//                    progressIcon.setVisible(false);
//                    checkButton.setEnabled(true);
//                    destroyButton.setEnabled(true);
//                    //show message
//                    parentWindow.showNotification("Failed: Could not find primary node");
//                } else {
                if (primaryNodeAgent != null && primaryNodeAgent.getUuid().compareTo(nodeAgent.getUuid()) != 0) {
                    Command unregisterSecondaryFromPrimaryCmd = op.peekNextTask().getCommands().iterator().next();
                    unregisterSecondaryFromPrimaryCmd.getRequest().setUuid(primaryNodeAgent.getUuid());
                } else {
                    //skip unregister command
                    op.getNextTask();
                }

                return op.getNextTask();
//                }
            }
        } else if (task.isCompleted()) {
            if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                if (op.hasNextTask()) {
                    return op.getNextTask();
                } else {
                    //update db
                    List<UUID> dataNodes = new ArrayList<UUID>(clusterInfo.getDataNodes());
                    for (Iterator<UUID> it = dataNodes.iterator(); it.hasNext();) {
                        UUID agentUUID = it.next();
                        if (agentUUID.compareTo(nodeAgent.getUuid()) == 0) {
                            it.remove();
                            break;
                        }
                    }
                    clusterInfo.setDataNodes(dataNodes);
                    MongoDAO.saveMongoClusterInfo(clusterInfo);
                    config.getDataNodes().remove(nodeAgent);

                    //update UI
                    dataNodesTable.removeItem(rowId);
                }
            } else {
                progressIcon.setVisible(false);
                checkButton.setEnabled(true);
                destroyButton.setEnabled(true);

                //show message
                parentWindow.showNotification(String.format("Failed task %s", task.getDescription()));
            }
        }

        return null;
    }

}
