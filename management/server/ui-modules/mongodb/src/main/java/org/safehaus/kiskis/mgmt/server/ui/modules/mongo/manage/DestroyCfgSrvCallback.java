/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Table;
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
public class DestroyCfgSrvCallback implements TaskCallback {

    private final Table nodesTable;
    private final Operation op;
    private final TaskRunner taskRunner;
    private final Button checkButton;
    private final Button destroyButton;
    private final Embedded progressIcon;

    public DestroyCfgSrvCallback(MongoClusterInfo clusterInfo, Table nodesTable, Operation op, TaskRunner taskRunner, Embedded progressIcon, Button checkButton, Button startButton, Button stopButton, Button destroyButton) {
        this.nodesTable = nodesTable;
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
        //uninstall
        //restart routers
        //adjust db        
        if (task.isCompleted()) {
            if (op.hasNextTask()) {
                taskRunner.runTask(op.getNextTask(), this);
            } else {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    //adjust db & UI
                } else {
                    progressIcon.setVisible(false);
                    checkButton.setEnabled(true);
                    destroyButton.setEnabled(true);
                }
            }
        }
    }

}
