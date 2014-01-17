/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;

/**
 *
 * @author dilshat
 */
public class CheckStatusCallback implements TaskCallback {

    private final TaskRunner taskRunner;
    private final Button startButton;
    private final Button stopButton;
    private final Embedded progressIcon;

    public CheckStatusCallback(TaskRunner taskRunner, Button startButton, Button stopButton, Embedded progressIcon) {
        this.taskRunner = taskRunner;
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.progressIcon = progressIcon;
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        progressIcon.setVisible(true);
    }

    private final StringBuilder stdOutput = new StringBuilder();
    private final StringBuilder stdErr = new StringBuilder();

    @Override
    public void onResponse(Task task, Response response) {
        if (!Util.isStringEmpty(response.getStdOut())) {
            stdOutput.append(response.getStdOut());
        }
        if (!Util.isStringEmpty(response.getStdErr())) {
            stdErr.append(response.getStdErr());
        }

        if (stdOutput.toString().contains("couldn't connect to server")) {
            startButton.setEnabled(true);
            progressIcon.setVisible(false);
            taskRunner.removeTaskCallback(task.getUuid());
        } else if (stdOutput.toString().contains("connecting to")) {
            stopButton.setEnabled(true);
            progressIcon.setVisible(false);
            taskRunner.removeTaskCallback(task.getUuid());
        } else if (stdErr.toString().contains("mongo: not found")) {
            progressIcon.setVisible(false);
            taskRunner.removeTaskCallback(task.getUuid());
        }
    }

}
