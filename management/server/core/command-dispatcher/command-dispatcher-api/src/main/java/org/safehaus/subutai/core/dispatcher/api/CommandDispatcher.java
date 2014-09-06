package org.safehaus.subutai.core.dispatcher.api;


import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;


/**
 * This class allows to send commands to remote Subutai and receive responses from it.
 *
 * Used internally by CommandRunner module.
 */
public interface CommandDispatcher {

    /**
     * Sends requests to remote peer Subutai
     *
     * @param requests - requests to send
     */
    public void sendRequests( Map<UUID, Request> requests );

    public void addListener( ResponseListener listener );

    public void removeListener( ResponseListener listener );

    /**
     * This method is called via REST only by remote peer Subutai.
     *
     * Caller will always be a remote peer Subutai supplying responses to requests previously sent to it.
     *
     * @param responses - responses to process
     */
    public void processResponse( Set<Response> responses );

    /**
     * This method is called via REST only by remote peer Subutai.
     *
     * It will execute requests on local agents and supply responses to remote peer Subutai.
     *
     * @param requests - requests to execute
     */
    public void executeRequests( UUID initiatorId, Set<Request> requests );
}
