package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Created by dilshat on 9/8/14.
 */
public class RemoteRequest {

    private final UUID commandId;
    private final long timestamp;
    private final String ip;
    private final int requestCount;
    private int attempts;
    private int requestsCompleted;


    public RemoteRequest( final String ip, final UUID commandId, final int requestsCount ) {
        Preconditions.checkNotNull( commandId, "CommandId is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ), "IP is null or empty" );
        Preconditions.checkArgument( requestsCount > 0, "Requests count is less than 0" );
        this.ip = ip;
        this.commandId = commandId;
        this.timestamp = System.currentTimeMillis();
        this.requestCount = requestsCount;
        attempts = 0;
    }


    public void incrementCompletedRequestsCount() {
        requestsCompleted++;
    }


    public boolean isCompleted() {
        return requestCount == requestsCompleted;
    }


    public String getIp() {
        return ip;
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
                "commandId=" + commandId +
                ", timestamp=" + timestamp +
                ", ip='" + ip + '\'' +
                ", requestCount=" + requestCount +
                ", attempts=" + attempts +
                ", requestsCompleted=" + requestsCompleted +
                '}';
    }
}
