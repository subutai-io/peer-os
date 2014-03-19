/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.callback;

import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class StartNodeCallback implements TaskCallback {

    private final Button checkButton;
    private final TaskRunner taskRunner;

    public StartNodeCallback(TaskRunner taskRunner, Embedded progressIcon, Button checkButton, Button startButton, Button stopButton, Button destroyButton) {
        this.taskRunner = taskRunner;
        progressIcon.setVisible(true);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        destroyButton.setEnabled(false);
        this.checkButton = checkButton;
    }

    @Override
    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
        if (stdOut.indexOf("child process started successfully, parent exiting") > -1) {
            taskRunner.removeTaskCallback(task.getUuid());
            checkButton.click();
        } else if (Util.isFinalResponse(response)) {
            checkButton.click();
        }

        return null;
    }

}
