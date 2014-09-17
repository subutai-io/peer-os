package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;

import com.google.common.base.Preconditions;


/**
 * Created by dilshat on 9/8/14.
 */
public class RemoteRequest {

    private final UUID peerId;
    private final UUID commandId;
    private final long timestamp;
    private final int requestCount;
    private int attempts;
    private int requestsCompleted;


    public RemoteRequest( final UUID peerId, final UUID commandId, final int requestsCount ) {
        Preconditions.checkNotNull( peerId, "Peer Id is null" );
        Preconditions.checkNotNull( commandId, "CommandId is null" );
        Preconditions.checkArgument( requestsCount > 0, "Requests count is less than 0" );
        this.peerId = peerId;
        this.commandId = commandId;
        this.timestamp = System.currentTimeMillis();
        this.requestCount = requestsCount;
        attempts = 0;
    }


    public UUID getPeerId() {
        return peerId;
    }


    public void incrementCompletedRequestsCount() {
        requestsCompleted++;
    }


    public boolean isCompleted() {
        return requestCount == requestsCompleted;
    }


    public long getTimestamp() {
        return timestamp;
    }


    public void incrementAttempts() {
        attempts++;
    }


    public UUID getCommandId() {
        return commandId;
    }


    public int getAttempts() {
        return attempts;
    }


    @Override
    public String toString() {
        return "RemoteRequest{" +
                "peerId=" + peerId +
                ", commandId=" + commandId +
                ", timestamp=" + timestamp +
                ", requestCount=" + requestCount +
                ", attempts=" + attempts +
                ", requestsCompleted=" + requestsCompleted +
                '}';
    }
}
