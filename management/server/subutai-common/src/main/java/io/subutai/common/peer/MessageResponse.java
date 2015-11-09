package io.subutai.common.peer;


import java.util.UUID;


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
        return "MessageResponse{" + "requestId=" + requestId + ", payload=" + payload + ", exception='" + exception
                + '\'' + '}';
    }
}
