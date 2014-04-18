/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.taskrunner.InterruptableTaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;

/**
 *
 * @author dilshat
 */
public class TaskRunnerFake implements TaskRunner {

    public void executeTask(Task task, TaskCallback taskCallback) {
        //not used
    }

    public Task executeTaskNWait(Task task) {
        if (task.getData() == TaskType.GET_LXC_LIST) {
            task.setCompleted(true);
            task.addResult(task.getRequests().iterator().next().getUuid(), new Result(TestUtils.getLxcListOutput(), null, 0));
        } else if (task.getData() == TaskType.CLONE_LXC) {
            task.setTaskStatus(TaskStatus.SUCCESS);
        } else if (task.getData() == TaskType.GET_METRICS) {
            task.setCompleted(true);
            task.addResult(task.getRequests().iterator().next().getUuid(), new Result(TestUtils.getMetricsOutput(), null, 0));
        }
        return task;
    }

    public void executeTaskNWait(Task task, TaskCallback callback) {
        if (task.getData() == TaskType.START_LXC) {
            task.setCompleted(true);
            Task getLxcInfoTask = callback.onResponse(task, null, null, null);
            getLxcInfoTask.setCompleted(true);
            callback.onResponse(getLxcInfoTask, null, "RUNNING", null);
        } else if (task.getData() == TaskType.STOP_LXC) {
            task.setCompleted(true);
            Task getLxcInfoTask = callback.onResponse(task, null, null, null);
            getLxcInfoTask.setCompleted(true);
            callback.onResponse(getLxcInfoTask, null, "STOPPED", null);
        } else if (task.getData() == TaskType.DESTROY_LXC) {
            task.setCompleted(true);
            Task getLxcInfoTask = callback.onResponse(task, null, null, null);
            getLxcInfoTask.setCompleted(true);
            getLxcInfoTask.setTaskStatus(TaskStatus.SUCCESS);
            callback.onResponse(getLxcInfoTask, null, null, null);
        } else if (task.getData() == TaskType.CLONE_N_START) {
            task.setCompleted(true);
            callback.onResponse(task, null, "RUNNING", null);
        }

    }

    public void executeTaskNWait(Task task, InterruptableTaskCallback callback) {
        //not used
    }

    public void removeTaskCallback(UUID taskUUID) {
        //not used
    }

}
