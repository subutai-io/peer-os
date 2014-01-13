/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;

/**
 *
 * @author dilshat
 */
public class ResponseProcessor {

    private final ExpiringCache<UUID, TaskListener> taskListenerCache = new ExpiringCache<UUID, TaskListener>();
    private final CommandManagerInterface commandManager;

    public ResponseProcessor() {
        this.commandManager = ServiceLocator.getService(CommandManagerInterface.class);
        if (commandManager == null) {
            throw new RuntimeException("Command manager is not available");
        }
    }

    public void feedResponse(Response response) {
        if (response != null && response.getTaskUuid() != null) {
            TaskListener tl = taskListenerCache.get(response.getTaskUuid());
            if (tl != null) {
                try {
                    tl.getResponseListener().onResponse(response);

                    if (Util.isFinalResponse(response)) {
                        if (tl.getTask().hasNextCommand()) {
                            commandManager.executeCommand(tl.getTask().getNextCommand());
                        } else {
                            taskListenerCache.remove(tl.getTask().getUuid());
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    public void executeTaskParallelly(Task task, ResponseListener responseListener) {
        if (task != null && task.getUuid() != null && responseListener != null) {
            if (taskListenerCache.get(task.getUuid()) == null && task.hasNextCommand()) {
                taskListenerCache.put(task.getUuid(),
                        new TaskListener(task, responseListener), task.getTotalTimeout() + 3000);
                while (task.hasNextCommand()) {
                    commandManager.executeCommand(task.getNextCommand());
                }
            } else {
                throw new RuntimeException("This task is already queued");
            }
        }
    }

    public void executeTaskSequentially(Task task, ResponseListener responseListener) {
        if (task != null && task.getUuid() != null && responseListener != null) {
            if (taskListenerCache.get(task.getUuid()) == null && task.hasNextCommand()) {
                taskListenerCache.put(task.getUuid(),
                        new TaskListener(task, responseListener), task.getTotalTimeout() + 3000);
                commandManager.executeCommand(task.getNextCommand());
            } else {
                throw new RuntimeException("This task is already queued");
            }
        }
    }
}
