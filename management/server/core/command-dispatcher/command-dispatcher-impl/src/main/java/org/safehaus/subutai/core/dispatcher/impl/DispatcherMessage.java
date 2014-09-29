package org.safehaus.subutai.core.dispatcher.impl;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Response;


/**
 * Object to be used for exchange with remote counterparts
 */
public class DispatcherMessage
{

    private final DispatcherMessageType dispatcherMessageType;

    private Set<BatchRequest> batchRequests;
    private Set<Response> responses;


    public DispatcherMessage( final DispatcherMessageType dispatcherMessageType, final Set<BatchRequest> batchRequests )
    {
        this.dispatcherMessageType = dispatcherMessageType;
        this.batchRequests = batchRequests;
    }


    public DispatcherMessage( final Set<Response> responses, final DispatcherMessageType dispatcherMessageType )
    {
        this.responses = responses;
        this.dispatcherMessageType = dispatcherMessageType;
    }


    public Set<BatchRequest> getBatchRequests()
    {
        return batchRequests;
    }


    public Set<Response> getResponses()
    {
        return responses;
    }


    public DispatcherMessageType getDispatcherMessageType()
    {
        return dispatcherMessageType;
    }
}
