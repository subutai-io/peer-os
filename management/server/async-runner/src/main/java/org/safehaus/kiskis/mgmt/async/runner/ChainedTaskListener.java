/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.async.runner;

import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ChainedTaskCallback;

/**
 *
 * @author dilshat
 */
public class ChainedTaskListener {

    private final Task task;
    private final ChainedTaskCallback taskCallback;

    public ChainedTaskListener(Task task, ChainedTaskCallback taskCallback) {
        this.task = task;
        this.taskCallback = taskCallback;
    }

    public Task getTask() {
        return task;
    }

    public ChainedTaskCallback getTaskCallback() {
        return taskCallback;
    }

}
