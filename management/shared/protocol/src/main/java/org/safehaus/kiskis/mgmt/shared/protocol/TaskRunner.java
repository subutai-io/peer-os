/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommunicationService;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 */
public class TaskRunner {

    private static final Logger LOG = Logger.getLogger(TaskRunner.class.getName());

    private final ExpiringCache<UUID, TaskListener> taskListenerCache = new ExpiringCache<UUID, TaskListener>();
    private CommandManager commandManager;
    private CommunicationService communicationService;

    public TaskRunner() {
        this.commandManager = ServiceLocator.getService(CommandManager.class);
        if (commandManager == null) {
            throw new RuntimeException("Command manager is not available");
        }
    }

    public TaskRunner(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    public Task feedResponse(Response response) {
        try {
            if (response != null && response.getTaskUuid() != null) {
                TaskListener tl = taskListenerCache.get(response.getTaskUuid());
                if (tl != null) {
                    if (Util.isFinalResponse(response)) {
                        tl.getTask().incrementCompletedCommandsCount();
                        if (response.getExitCode() != null && response.getExitCode() == 0) {
                            tl.getTask().incrementSucceededCommandsCount();
                        }
                        if (tl.getTask().getCompletedCommandsCount() == tl.getTask().getLaunchedCommandsCount()) {
                            tl.getTask().setCompleted(true);
                            if (tl.getTask().isIgnoreExitCode()
                                    || tl.getTask().getCompletedCommandsCount() == tl.getTask().getSucceededCommandsCount()) {
                                tl.getTask().setTaskStatus(TaskStatus.SUCCESS);
                            } else {
                                tl.getTask().setTaskStatus(TaskStatus.FAIL);
                            }
                            taskListenerCache.remove(tl.getTask().getUuid());
                        }
                    }

                    tl.getTaskCallback().onResponse(tl.getTask(), response);

                    return tl.getTask();
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error processing response: {0}", e);
        }
        return null;
    }

    public Task getTask(UUID taskUUID) {
        if (taskUUID != null) {
            TaskListener tl = taskListenerCache.get(taskUUID);
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

    public int getRemainingTaskCount() {
        return taskListenerCache.size();
    }

    public void runTask(Task task, TaskCallback taskCallback) {
        try {
            if (task != null && task.getUuid() != null) {
                if (taskListenerCache.get(task.getUuid()) == null && task.hasNextCommand()) {
                    if (taskCallback != null) {
                        taskListenerCache.put(task.getUuid(),
                                new TaskListener(task, taskCallback), task.getAvgTimeout() * 1000 + 10000);
                    }
                    while (task.hasNextCommand()) {
                        if (commandManager != null) {
                            commandManager.executeCommand(task.getNextCommand());
                        } else {
                            communicationService.sendCommand(task.getNextCommand());
                        }
                    }
                } else {
                    throw new RuntimeException("This task is already queued or has no more commands");
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error running task: {0}", e);
        }
    }

}
