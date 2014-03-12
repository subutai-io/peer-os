/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.api.communication.Communication;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 */
public class ChainedTaskRunner {

    private static final Logger LOG = Logger.getLogger(ChainedTaskRunner.class.getName());

    private final ExpiringCache<UUID, ChainedTaskListener> taskListenerCache = new ExpiringCache<UUID, ChainedTaskListener>();
    private final Communication communicationService;

    public ChainedTaskRunner(Communication communicationService) {
        this.communicationService = communicationService;
    }

    public ChainedTaskListener feedResponse(Response response) {
        try {
            if (response != null && response.getTaskUuid() != null && response.getUuid() != null && response.getType() != null) {
                ChainedTaskListener tl = taskListenerCache.get(response.getTaskUuid());
                if (tl != null) {

                    tl.appendStreams(response);

                    if (Util.isFinalResponse(response)) {
                        tl.getTask().incrementCompletedRequestsCount();
                        if (response.getExitCode() != null && response.getExitCode() == 0) {
                            tl.getTask().incrementSucceededRequestsCount();
                        }
                        if (tl.getTask().getCompletedRequestsCount() == tl.getTask().getLaunchedRequestsCount()) {
                            tl.getTask().setCompleted(true);
                            if (tl.getTask().isIgnoreExitCode()
                                    || tl.getTask().getCompletedRequestsCount() == tl.getTask().getSucceededRequestsCount()) {
                                tl.getTask().setTaskStatus(TaskStatus.SUCCESS);
                            } else {
                                tl.getTask().setTaskStatus(TaskStatus.FAIL);
                            }
                            taskListenerCache.remove(tl.getTask().getUuid());
                        }
                    }

                    Task nextTask = tl.getTaskCallback().onResponse(tl.getTask(), response, tl.getStdOut(response), tl.getStdErr(response));

                    return nextTask == null ? tl : new ChainedTaskListener(nextTask, tl.getTaskCallback());
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, String.format("Error processing response: %s"), e);
        }
        return null;
    }

    public Task getTask(UUID taskUUID) {
        if (taskUUID != null) {
            ChainedTaskListener tl = taskListenerCache.get(taskUUID);
            if (tl != null) {
                return tl.getTask();
            }
        }
        return null;
    }

    public void removeTaskCallback(UUID taskUUID) {
        if (taskUUID != null) {
            taskListenerCache.remove(taskUUID);
        }
    }

    public void removeAllTaskCallbacks() {
        taskListenerCache.clear();
    }

    public void executeTask(Task task, TaskCallback taskCallback) {
        try {
            if (task != null && task.getUuid() != null) {
                if (taskListenerCache.get(task.getUuid()) == null && task.hasNextRequest()) {
                    if (taskCallback != null) {
                        taskListenerCache.put(task.getUuid(),
                                new ChainedTaskListener(task, taskCallback), task.getAvgTimeout() * 1000 + 10000);
                    }
                    while (task.hasNextRequest()) {
                        communicationService.sendRequest(task.getNextRequest());
                    }
                } else {
                    throw new Exception("This task is already queued or has no more commands");
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, String.format("Error running task: %s"), e);
        }
    }

}
