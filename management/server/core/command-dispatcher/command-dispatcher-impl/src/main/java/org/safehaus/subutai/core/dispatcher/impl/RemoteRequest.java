package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;


/**
 * Created by dilshat on 9/8/14.
 */
public class RemoteRequest {

    private final UUID ownerId;
    private final UUID commandId;
    private final long timestamp;
    private final String ip;
    private int attempts;


    public RemoteRequest( final String ip, final UUID ownerId, final UUID commandId ) {
        this.ip = ip;
        this.ownerId = ownerId;
        this.commandId = commandId;
        this.timestamp = System.currentTimeMillis();
        attempts = 0;
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


    public UUID getOwnerId() {
        return ownerId;
    }


    public int getAttempts() {
        return attempts;
    }


    @Override
    public String toString() {
        return "RemoteRequest{" +
                "ownerId=" + ownerId +
                ", commandId=" + commandId +
                ", timestamp=" + timestamp +
                ", attempts=" + attempts +
                '}';
    }
}
