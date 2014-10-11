package org.safehaus.subutai.core.dispatcher.impl;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.CollectionUtil;

import com.google.common.base.Preconditions;


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
        Preconditions.checkNotNull( dispatcherMessageType, "Message Type is null" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( batchRequests ), "Requests are empty" );

        this.dispatcherMessageType = dispatcherMessageType;
        this.batchRequests = batchRequests;
    }


    public DispatcherMessage( final Set<Response> responses, final DispatcherMessageType dispatcherMessageType )
    {
        Preconditions.checkNotNull( dispatcherMessageType, "Message Type is null" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( responses ), "Responses are empty" );

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
