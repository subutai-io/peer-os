/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.ClusterConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.ClusterDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.MongoClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Operation;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 */
public class DestroyRouterCallback implements TaskCallback {

    private final Window parentWindow;
    private final MongoClusterInfo clusterInfo;
    private final ClusterConfig config;
    private final Agent nodeAgent;
    private final Table routersTable;
    private final Object rowId;
    private final Operation op;
    private final TaskRunner taskRunner;
    private final Button checkButton;
    private final Button destroyButton;
    private final Embedded progressIcon;

    public DestroyRouterCallback(Window parentWindow, MongoClusterInfo clusterInfo, ClusterConfig config, Agent nodeAgent, Table routersTable, Object rowId, Operation op, TaskRunner taskRunner, Embedded progressIcon, Button checkButton, Button startButton, Button stopButton, Button destroyButton) {
        this.parentWindow = parentWindow;
        this.clusterInfo = clusterInfo;
        this.config = config;
        this.nodeAgent = nodeAgent;
        this.routersTable = routersTable;
        this.rowId = rowId;
        this.op = op;
        this.taskRunner = taskRunner;
        this.checkButton = checkButton;
        this.destroyButton = destroyButton;
        this.progressIcon = progressIcon;
        progressIcon.setVisible(true);
        checkButton.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        destroyButton.setEnabled(false);
    }

    @Override
    public void onResponse(Task task, Response response) {
        if (task.isCompleted()) {
            if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                if (op.hasNextTask()) {
                    taskRunner.runTask(op.getNextTask(), this);
                } else {
                    //update db
                    List<UUID> routers = new ArrayList<UUID>(clusterInfo.getRouters());
                    for (Iterator<UUID> it = routers.iterator(); it.hasNext();) {
                        UUID agentUUID = it.next();
                        if (agentUUID.compareTo(nodeAgent.getUuid()) == 0) {
                            it.remove();
                            break;
                        }
                    }
                    clusterInfo.setRouters(routers);
                    ClusterDAO.saveMongoClusterInfo(clusterInfo);
                    config.getRouterServers().remove(nodeAgent);

                    //update UI
                    routersTable.removeItem(rowId);
                }
            } else {
                progressIcon.setVisible(false);
                checkButton.setEnabled(true);
                destroyButton.setEnabled(true);

                //show message
                parentWindow.showNotification(String.format("Failed task %s", task.getDescription()));
            }
        }
    }

}
