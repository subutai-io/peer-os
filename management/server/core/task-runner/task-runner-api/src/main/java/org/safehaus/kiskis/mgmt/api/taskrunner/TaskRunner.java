/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.taskrunner;

import java.util.UUID;

/**
 * This class is used to execute tasks on agent nodes. TODO rename public Task
 * executeTask(Task task) to executeTaskNWait
 *
 * @author dilshat
 */
public interface TaskRunner {

    /**
     * Used when sending requests to indicate that commands are sent by task
     * runner module
     */
    public static String MODULE_NAME = "TaskRunner";

    /**
     * Executes {@code Task} asynchronously to the calling party. The supplied
     * {@code TaskCallBack} is triggered every time a response is received for
     * this task. If null for callback is supplied this call is the same as
     * calling executeTaskNForget.
     *
     * @param task - task to execute
     * @param taskCallback - callback to trigger when response from agent is
     * received
     */
    public void executeTask(Task task, TaskCallback taskCallback);

    /**
     * Executes {@code Task} synchronously to the calling party. The method
     * returns when either task is completed or timed out. This method waits 1
     * hour maximum and them times out. Calling party should examine the
     * returned/supplied task to see its status after this method returns
     *
     * @param task - task to execute
     * @return task which is supplied when calling this method;
     */
    public Task executeTaskNWait(Task task);

    /**
     * Executes {@code Task} synchronously to the calling party. The method
     * returns when either task is completed or timed out. This method waits 1
     * hour maximum and them times out. Calling party should examine the
     * returned/supplied task to see its status after this method returns.
     *
     * @param task - task to execute
     * @param callback - task callback
     */
    public void executeTaskNWait(Task task, TaskCallback callback);

    /**
     * Executes {@code Task} synchronously to the calling party. The method
     * returns when either task is completed or timed out. This method waits 1
     * hour maximum and them times out. Calling party should examine the
     * returned/supplied task to see its status after this method returns.
     *
     * @param task - task to execute
     * @param callback - task callback
     */
    public void executeTaskNWait(Task task, InterruptableTaskCallback callback);

    /**
     * Removes callback for a task if any with the supplied UUID. Should be used
     * only with asynchronous executeTask for pulling current callback out of
     * the processing pipe of taskrunner to stop processing the task or to
     * supply new task for processing via return new Task();
     *
     * @param taskUUID - uuid of the task
     */
    public void removeTaskCallback(UUID taskUUID);

}
