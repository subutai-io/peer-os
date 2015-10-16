package io.subutai.core.peer.impl.request;


import java.util.UUID;

import io.subutai.core.peer.api.Payload;


public class MessageResponse
{
    private UUID requestId;
    private Payload payload;
    private String exception;


    public MessageResponse( final UUID requestId, final Payload payload, final String exception )
    {
        this.requestId = requestId;
        this.payload = payload;
        this.exception = exception;
    }


    public UUID getRequestId()
    {
        return requestId;
    }


    public Payload getPayload()
    {
        return payload;
    }


    public String getException()
    {
        return exception;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "MessageResponse{" );
        sb.append( "requestId=" ).append( requestId );
        sb.append( ", payload=" ).append( payload );
        sb.append( ", exception='" ).append( exception ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}
