/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.taskrunner;

import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public abstract class InterruptableTaskCallback implements TaskCallback {

    public abstract Task onResponse(Task task, Response response, String stdOut, String stdErr);

    public final void interrupt() {
        synchronized (this) {
            notifyAll();
        }
    }
}
