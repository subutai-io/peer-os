package org.safehaus.subutai.core.communication.api;


import org.safehaus.subutai.common.protocol.Response;


/**
 * This interface must be implemented to receive responses from agents.
 */
public interface ResponseListener {

	/**
	 * Response arrival event
	 *
	 * @param response - received response
	 */
	public void onResponse(Response response);
}
