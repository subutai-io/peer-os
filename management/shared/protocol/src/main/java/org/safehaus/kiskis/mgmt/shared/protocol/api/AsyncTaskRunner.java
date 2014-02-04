/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.api;

import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 *
 * @author dilshat
 */
public interface AsyncTaskRunner {

    public void executeTask(Task task, TaskCallback taskCallback);

    public void removeTaskCallback(UUID taskUUID);
}
