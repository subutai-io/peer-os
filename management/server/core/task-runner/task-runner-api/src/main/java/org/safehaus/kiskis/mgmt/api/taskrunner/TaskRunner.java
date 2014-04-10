/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.taskrunner;

import java.util.UUID;

/**
 * This class is used to execute tasks on agent nodes
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
     * returns when either task is completed or timed out. Calling party should
     * examine the returned/supplied task to see its status after this method
     * returns
     *
     * @param task - task to execute
     * @return task which is supplied when calling this method;
     */
    public Task executeTask(Task task);

    /**
     * Executes supplied task asynchronously to the calling party.
     *
     * @param task
     */
    public void executeTaskNForget(Task task);

    /**
     * Removes callback for a task if any with the supplied UUID
     *
     * @param taskUUID - uuid of the task
     */
    public void removeTaskCallback(UUID taskUUID);

}
