package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;

import com.google.common.base.Preconditions;


/**
 * Wrapper around Request from remote peer
 */
public class RemoteRequest
{

    private final UUID peerId;
    private final UUID commandId;
    private final int requestCount;
    private long timestamp;
    private int attempts;
    private int requestsCompleted;


    public RemoteRequest( final UUID peerId, final UUID commandId, final int requestsCount )
    {
        Preconditions.checkNotNull( peerId, "Peer Id is null" );
        Preconditions.checkNotNull( commandId, "CommandId is null" );
        Preconditions.checkArgument( requestsCount > 0, "Requests count is less than 0" );
        this.peerId = peerId;
        this.commandId = commandId;
        this.timestamp = System.currentTimeMillis();
        this.requestCount = requestsCount;
        attempts = 1;
    }


    public UUID getPeerId()
    {
        return peerId;
    }


    public void incrementCompletedRequestsCount()
    {
        requestsCompleted++;
    }


    public boolean isCompleted()
    {
        return requestCount == requestsCompleted;
    }


    public long getTimestamp()
    {
        return timestamp;
    }


    public void incrementAttempts()
    {
        attempts++;
    }


    public UUID getCommandId()
    {
        return commandId;
    }


    public void updateTimestamp()
    {
        this.timestamp = System.currentTimeMillis();
    }


    public int getAttempts()
    {
        return attempts;
    }
}
