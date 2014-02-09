/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;

/**
 *
 * @author dilshat
 */
public class TaskListener {

    private final Task task;
    private final TaskCallback taskCallback;

    public TaskListener(Task task, TaskCallback taskCallback) {
        this.task = task;
        this.taskCallback = taskCallback;
    }

    public Task getTask() {
        return task;
    }

    public TaskCallback getTaskCallback() {
        return taskCallback;
    }

}
