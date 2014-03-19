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
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Config;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.MongoDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.taskrunner.Operation;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;

/**
 *
 * @author dilshat
 */
public class DestroyRouterCallback implements TaskCallback {

    private final Window parentWindow;
    private final Config config;
    private final Agent nodeAgent;
    private final Table routersTable;
    private final Object rowId;
    private final Operation op;
    private final Button checkButton;
    private final Button destroyButton;
    private final Embedded progressIcon;

    public DestroyRouterCallback(Window parentWindow, Config config, Agent nodeAgent, Table routersTable, Object rowId, Operation op, Embedded progressIcon, Button checkButton, Button startButton, Button stopButton, Button destroyButton) {
        this.parentWindow = parentWindow;
        this.config = config;
        this.nodeAgent = nodeAgent;
        this.routersTable = routersTable;
        this.rowId = rowId;
        this.op = op;
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
    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
        if (task.isCompleted()) {
            if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                if (op.hasNextTask()) {
                    return op.getNextTask();
                } else {
                    //update db
                    config.getRouterServers().remove(nodeAgent);
                    MongoDAO.saveMongoClusterInfo(config);

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

        return null;
    }

}
