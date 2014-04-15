package org.safehaus.kiskis.mgmt.api.communicationmanager;

import org.safehaus.kiskis.mgmt.shared.protocol.Request;

/**
 * Interface for communication manager implementations.
 *
 * @author dilshat
 */
public interface CommunicationManager {

    /**
     * Sends request to agent
     *
     * @param request
     */
    public void sendRequest(Request request);

    /**
     * adds response listener
     *
     * @param listener
     */
    public void addListener(ResponseListener listener);

    /**
     * removes response listener
     *
     * @param listener
     */
    public void removeListener(ResponseListener listener);
}
