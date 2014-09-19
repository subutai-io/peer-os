package org.safehaus.subutai.core.communication.api;


import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.ResponseListener;


/**
 * Interface for communication manager implementations.
 */
public interface CommunicationManager
{

    /**
     * Sends request to agent
     *
     * @param request - request to send
     */
    public void sendRequest( Request request );

    public void sendBroadcastRequest( Request request );

    /**
     * adds response listener
     *
     * @param listener - listener to add
     */
    public void addListener( ResponseListener listener );

    /**
     * removes response listener
     *
     * @param listener - - listener to remove
     */
    public void removeListener( ResponseListener listener );
}
