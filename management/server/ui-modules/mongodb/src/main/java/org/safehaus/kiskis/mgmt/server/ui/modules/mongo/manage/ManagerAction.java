/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.ui.Button;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class ManagerAction {

    private final Task task;
    private final Button startButton;
    private final Button stopButton;
    private final ManagerActionType managerActionType;
    private final StringBuilder output = new StringBuilder();

    public ManagerAction(Task task, Button startButton, Button stopButton, ManagerActionType managerActionType) {
        this.task = task;
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.managerActionType = managerActionType;
    }

    public Task getTask() {
        return task;
    }

    public Button getStartButton() {
        return startButton;
    }

    public Button getStopButton() {
        return stopButton;
    }

    public ManagerActionType getManagerActionType() {
        return managerActionType;
    }

    public void addOutput(String out) {
        if (!Util.isStringEmpty(out)) {
            output.append(out);
        }
    }

    public String getOutput() {
        return output.toString();
    }

}
