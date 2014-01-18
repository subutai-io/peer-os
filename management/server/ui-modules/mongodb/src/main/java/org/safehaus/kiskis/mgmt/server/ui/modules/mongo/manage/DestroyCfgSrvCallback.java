/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Table;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.TaskType;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.ClusterDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.MongoClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 */
public class DestroyCfgSrvCallback implements TaskCallback {

    private final MongoClusterInfo clusterInfo;
    private final ClusterConfig config;
    private final Agent nodeAgent;
    private final Table cfgSrvTable;
    private final Table routersTable;
    private final Object rowId;
    private final Operation op;
    private final TaskRunner taskRunner;
    private final Button checkButton;
    private final Button destroyButton;
    private final Embedded progressIcon;
    private final StringBuilder stdOutput = new StringBuilder();

    public DestroyCfgSrvCallback(MongoClusterInfo clusterInfo, ClusterConfig config, Agent nodeAgent, Table cfgSrvTable, Table routersTable, Object rowId, Operation op, TaskRunner taskRunner, Embedded progressIcon, Button checkButton, Button startButton, Button stopButton, Button destroyButton) {
        this.clusterInfo = clusterInfo;
        this.config = config;
        this.nodeAgent = nodeAgent;
        this.cfgSrvTable = cfgSrvTable;
        this.routersTable = routersTable;
        this.rowId = rowId;
        this.op = op;
        this.taskRunner = taskRunner;
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
    public void onResponse(Task task, Response response) {
        if (task.getData() == TaskType.START_ROUTERS
                && !Util.isStringEmpty(response.getStdOut())) {
            stdOutput.append(response.getStdOut());
            if (Util.countNumberOfOccurences(stdOutput.toString(), "child process started successfully, parent exiting")
                    == clusterInfo.getRouters().size()) {
                task.setTaskStatus(TaskStatus.SUCCESS);
                task.setCompleted(true);
                taskRunner.removeTaskCallback(task.getUuid());
            }
        }
        if (task.isCompleted()) {
            if (op.hasNextTask()) {
                taskRunner.runTask(op.getNextTask(), this);
            } else {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    //update db

                    List<UUID> configServers = new ArrayList<UUID>(clusterInfo.getConfigServers());
                    for (Iterator<UUID> it = configServers.iterator(); it.hasNext();) {
                        UUID agentUUID = it.next();
                        if (agentUUID.compareTo(nodeAgent.getUuid()) == 0) {
                            it.remove();
                            break;
                        }
                    }
                    clusterInfo.setConfigServers(configServers);
                    ClusterDAO.saveMongoClusterInfo(clusterInfo);
                    config.getConfigServers().remove(nodeAgent);
                    //update UI
                    cfgSrvTable.removeItem(rowId);
                } else {
                    progressIcon.setVisible(false);
                    checkButton.setEnabled(true);
                    destroyButton.setEnabled(true);
                }
                //check statuses of routers
                Manager2.checkNodesStatus(routersTable);
            }
        }
    }

}
