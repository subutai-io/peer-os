package org.safehaus.kiskis.mgmt.api.communicationmanager;

import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 * This interface must be implemented to receive responses from agents.
 *
 * @author dilshat
 */
public interface ResponseListener {

    /**
     * Response arrival event
     *
     * @param response
     */
    public void onResponse(Response response);

}
