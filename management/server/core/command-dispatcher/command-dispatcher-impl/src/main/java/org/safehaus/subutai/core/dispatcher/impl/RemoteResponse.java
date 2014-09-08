package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Response;

import com.google.common.base.Preconditions;


/**
 * Created by dilshat on 9/8/14.
 */
public class RemoteResponse {

    private final UUID ownerId;
    private final UUID commandId;
    private final Response response;


    public RemoteResponse( final UUID ownerId, final UUID commandId, final Response response ) {
        Preconditions.checkNotNull( ownerId, "Owner Id is null" );
        Preconditions.checkNotNull( commandId, "Command Id is null" );
        Preconditions.checkNotNull( response, "Response is null" );

        this.ownerId = ownerId;
        this.commandId = commandId;
        this.response = response;
    }


    public UUID getOwnerId() {
        return ownerId;
    }


    public UUID getCommandId() {
        return commandId;
    }


    public Response getResponse() {
        return response;
    }
}
