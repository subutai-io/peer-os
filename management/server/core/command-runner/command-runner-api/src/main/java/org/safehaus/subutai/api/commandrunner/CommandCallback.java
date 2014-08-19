/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.commandrunner;


import org.safehaus.subutai.shared.protocol.Response;


/**
 * Command callback triggered every time a response is received from agent
 */
public class CommandCallback {

	private volatile boolean stopped;


	/**
	 * This method is triggered every time a response is received from agent
	 *
	 * @param response    - the original response from agent
	 * @param agentResult - the cumulated result from an agent which sent this response
	 * @param command     - the original command which was executed for on agent
	 */
	public void onResponse(Response response, AgentResult agentResult, Command command) {
	}


	/**
	 * Causes the call to runCommand to return, associated callback (if any) no longer receives responses from agent
	 * even (if any). Command status is not changed by this call
	 */
	public final void stop() {
		stopped = true;
	}


	public final boolean isStopped() {
		return stopped;
	}
}
