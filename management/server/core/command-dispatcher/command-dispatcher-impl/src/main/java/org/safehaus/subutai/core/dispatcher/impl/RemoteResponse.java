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
    private final UUID agentId;
    private final Response response;


    public RemoteResponse( final UUID ownerId, final Response response ) {
        Preconditions.checkNotNull( ownerId, "Owner Id is null" );
        Preconditions.checkNotNull( response, "Response is null" );

        this.ownerId = ownerId;
        this.commandId = response.getTaskUuid();
        this.agentId = response.getUuid();
        this.response = response;
    }


    public UUID getAgentId() {
        return agentId;
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
