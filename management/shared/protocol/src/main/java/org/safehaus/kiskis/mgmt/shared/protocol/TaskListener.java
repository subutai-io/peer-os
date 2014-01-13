/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;

/**
 *
 * @author dilshat
 */
public class TaskListener {

    private final Task task;
    private final ResponseListener responseListener;

    public TaskListener(Task task, ResponseListener responseListener) {
        this.task = task;
        this.responseListener = responseListener;
    }

    public Task getTask() {
        return task;
    }

    public ResponseListener getResponseListener() {
        return responseListener;
    }

}
