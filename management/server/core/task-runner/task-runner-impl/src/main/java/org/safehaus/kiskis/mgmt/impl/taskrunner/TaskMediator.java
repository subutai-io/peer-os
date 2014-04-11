/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.api.communicationmanager.CommunicationManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.Result;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;

/**
 * This class is used internally by {@code TaskRunner}. {@code TaskMediator}
 * acts as a mediator to communication manager service. It sends all requests to
 * and processes all responses from agents routing them to corresponding
 * {@code TaskListener} For this purpose it holds a map of all
 * {@code TaskListener}.
 *
 * @author dilshat
 */
class TaskMediator {

    private static final Logger LOG = Logger.getLogger(TaskMediator.class.getName());

    /**
     * Map of {@code TaskListener} where key is UUID of agent/node
     */
    private final ExpiringCache<UUID, TaskListener> taskListenerCache;
    /**
     * reference to communication manager service
     */
    private final CommunicationManager communicationService;

    /**
     * Initializes {@code TaskMediator}
     *
     * @param communicationService
     */
    public TaskMediator(CommunicationManager communicationService, ExecutorService executor) {
        this.communicationService = communicationService;
        taskListenerCache = new ExpiringCache<UUID, TaskListener>(executor);
    }

    /**
     * This method is used to feed response coming from agent(s) for processing
     * by corresponding {@code TaskListener}.
     *
     * @param response
     * @return null, current {@code TaskListener} of new {@code TaskListener} if
     * corresponding {@code TaskCallback} returned new task for execution.
     */
    public TaskListener feedResponse(Response response) {
        try {
            if (response != null && response.getTaskUuid() != null && response.getUuid() != null && response.getType() != null) {
                TaskListener tl = taskListenerCache.get(response.getTaskUuid());
                if (tl != null) {

                    tl.appendStreams(response);
                    tl.getTask().addResult(response.getUuid(), new Result(tl.getStdOut(response), tl.getStdErr(response), response.getExitCode()));

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

                    if (nextTask == null && tl.getTask().isCompleted()) {
                        //notify in case someone is waiting on this task callback
                        synchronized (tl.getTaskCallback()) {
                            tl.getTaskCallback().notifyAll();
                        }
                    }

                    return nextTask == null ? tl : new TaskListener(nextTask, tl.getTaskCallback());
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, String.format("Error processing response: %s"), e);
        }
        return null;
    }

    /**
     * Returns task by its UUID or null
     *
     * @param taskUUID
     * @return {@code Task}
     */
    public Task getTask(UUID taskUUID) {
        if (taskUUID != null) {
            TaskListener tl = taskListenerCache.get(taskUUID);
            if (tl != null) {
                return tl.getTask();
            }
        }
        return null;
    }

    /**
     * Removes {@code TaskCallback} by its task's UUID
     *
     * @param taskUUID
     */
    public void removeTaskCallback(UUID taskUUID) {
        if (taskUUID != null) {
            taskListenerCache.remove(taskUUID);
        }
    }

    /**
     * Removes all {@code TaskCallback}
     */
    public void removeAllTaskCallbacks() {
        taskListenerCache.clear();
    }

    /**
     * Submits {@code Task} for execution. If task is not completed after its
     * timeout {@code task.getAverageTimeout()} interval, its status is set as
     * TIMEDOUT.
     *
     * @param task - task to execute
     * @param taskCallback - callback or null
     */
    public void executeTask(Task task, TaskCallback taskCallback) {
        try {
            if (task != null && task.getUuid() != null) {
                if (taskListenerCache.get(task.getUuid()) == null && task.hasNextRequest()) {
                    task.setTaskStatus(TaskStatus.RUNNING);
                    if (taskCallback != null) {
                        taskListenerCache.put(task.getUuid(),
                                new TaskListener(task, taskCallback), task.getAvgTimeout() * 1000 + 500, new EntryExpiryCallback<TaskListener>() {

                                    public void onEntryExpiry(TaskListener entry) {
                                        Task task = entry.getTask();
                                        if (task.getTaskStatus() == TaskStatus.RUNNING) {
                                            task.setTaskStatus(TaskStatus.TIMEDOUT);
                                        }
                                        //notify in case someone is waiting on this task callback
                                        synchronized (entry.getTaskCallback()) {
                                            entry.getTaskCallback().notifyAll();
                                        }
                                    }
                                });
                    }
                    while (task.hasNextRequest()) {
                        communicationService.sendRequest(task.getNextRequest());
                    }
                } else {
                    throw new Exception("This task is already queued or has no more commands");
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, String.format("Error running task: %s", e), e);
        }
    }

}
