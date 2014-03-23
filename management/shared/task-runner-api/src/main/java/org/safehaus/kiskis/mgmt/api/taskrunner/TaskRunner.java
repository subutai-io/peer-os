/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.taskrunner;

import java.util.UUID;

/**
 *
 * @author dilshat
 */
public interface TaskRunner {

    public void executeTask(Task task, TaskCallback taskCallback);

    public Task executeTask(Task task);

    public void executeTaskNForget(Task task);

    public void removeTaskCallback(UUID taskUUID);
}
