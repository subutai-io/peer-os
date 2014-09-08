package org.safehaus.subutai.core.dispatcher.impl;


import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Response;

import com.google.common.base.Preconditions;


/**
 * Created by dilshat on 9/8/14.
 */
public class RemoteResponse {

    private final UUID ownerId;
    private final UUID commandId;
    private final Set<Response> responses = new LinkedHashSet<>();
    private int attempts = 0;


    public RemoteResponse( final UUID ownerId, final UUID commandId ) {
        Preconditions.checkNotNull( ownerId, "Owner Id is null" );
        Preconditions.checkNotNull( commandId, "Command Id is null" );

        this.ownerId = ownerId;
        this.commandId = commandId;
    }


    public UUID getOwnerId() {
        return ownerId;
    }


    public UUID getCommandId() {
        return commandId;
    }


    public void incrementAttempts() {
        attempts++;
    }


    public int getAttempts() {
        return attempts;
    }


    public void addResponse( Response response ) {
        Preconditions.checkNotNull( response, "Response is null" );

        responses.add( response );
    }


    public Set<Response> getResponses() {
        return Collections.unmodifiableSet( responses );
    }
}
