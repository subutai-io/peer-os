/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.taskrunner;

import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 * This interface should be implemented for supplying callbacks to
 * TaskRunner.executeTask(Task task, TaskCallback taskCallback)
 *
 * @author dilshat
 */
public interface TaskCallback {

    /**
     *
     * @param task the task submitted for execution
     * @param response response from this particular agent/node where UUID of
     * node can be retrieved from response.getUUID()
     * @param stdOut cumulated last 10000 symbols of std out from this
     * particular agent/node
     * @param stdErr cumulated last 10000 symbols of std err from this
     * particular agent/node
     * @return null or next task to be executed. If task is returned then it is
     * immediately submitted for processing and responses for the previous task
     * are ignored
     */
    public Task onResponse(Task task, Response response, String stdOut, String stdErr);
}
