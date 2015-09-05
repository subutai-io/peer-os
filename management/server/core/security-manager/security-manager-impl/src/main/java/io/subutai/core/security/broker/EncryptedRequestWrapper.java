package io.subutai.core.security.broker;


import java.util.UUID;


public class EncryptedRequestWrapper
{
    private final String request;
    private final UUID hostId;


    public EncryptedRequestWrapper( final String request, final UUID hostId )
    {
        this.request = request;
        this.hostId = hostId;
    }
}
