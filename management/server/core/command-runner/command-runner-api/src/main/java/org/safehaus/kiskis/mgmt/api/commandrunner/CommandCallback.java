/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.commandrunner;

import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public class CommandCallback {

    private volatile boolean stopped;

    public void onResponse(Response response, AgentResult agentResult, Command command) {
    }

    public final void stop() {
        stopped = true;
    }

    public final boolean isStopped() {
        return stopped;
    }

}
