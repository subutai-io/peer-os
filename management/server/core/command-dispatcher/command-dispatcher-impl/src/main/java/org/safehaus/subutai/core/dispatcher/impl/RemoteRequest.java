package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;


/**
 * Created by dilshat on 9/8/14.
 */
public class RemoteRequest {

    private final UUID ownerId;
    private final UUID commandId;
    private int attempts;


    public RemoteRequest( final UUID ownerId, final UUID commandId ) {
        this.ownerId = ownerId;
        this.commandId = commandId;
        attempts = 0;
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
}
