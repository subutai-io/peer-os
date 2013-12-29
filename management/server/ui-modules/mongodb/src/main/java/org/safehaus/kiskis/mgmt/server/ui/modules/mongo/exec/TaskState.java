/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec;

/**
 *
 * @author dilshat
 */
public class TaskState {

    private boolean completed = false;
    private boolean successfull = false;

    public boolean isCompleted() {
        return completed;
    }

    public boolean isSuccessfull() {
        return successfull;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setSuccessfull(boolean successfull) {
        this.successfull = successfull;
    }

}
