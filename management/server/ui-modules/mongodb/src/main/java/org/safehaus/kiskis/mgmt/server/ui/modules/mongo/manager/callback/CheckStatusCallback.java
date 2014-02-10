/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.callback;

import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AsyncTaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ChainedTaskCallback;

/**
 *
 * @author dilshat
 */
public class CheckStatusCallback implements ChainedTaskCallback {

    private final AsyncTaskRunner taskRunner;
    private final Embedded progressIcon;
    private final Button startButton;
    private final Button stopButton;
    private final Button destroyButton;

    public CheckStatusCallback(AsyncTaskRunner taskRunner, Embedded progressIcon, Button startButton, Button stopButton, Button destroyButton) {
        this.taskRunner = taskRunner;
        this.progressIcon = progressIcon;
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.destroyButton = destroyButton;
        progressIcon.setVisible(true);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        destroyButton.setEnabled(false);
    }

    @Override
    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

        if (stdOut.indexOf("couldn't connect to server") > -1) {
            startButton.setEnabled(true);
            destroyButton.setEnabled(true);
            progressIcon.setVisible(false);
            taskRunner.removeTaskCallback(task.getUuid());
        } else if (stdOut.indexOf("connecting to") > -1) {
            stopButton.setEnabled(true);
            destroyButton.setEnabled(true);
            progressIcon.setVisible(false);
            taskRunner.removeTaskCallback(task.getUuid());
        } else if (stdErr.indexOf("mongo: not found") > -1) {
            destroyButton.setEnabled(true);
            progressIcon.setVisible(false);
            taskRunner.removeTaskCallback(task.getUuid());
        } else if (Util.isFinalResponse(response)) {
            destroyButton.setEnabled(true);
            progressIcon.setVisible(false);
        }

        return null;
    }

}
