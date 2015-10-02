package io.subutai.core.security.broker;


public class EncryptedRequestWrapper
{
    private final String request;
    private final String hostId;


    public EncryptedRequestWrapper( final String request, final String hostId )
    {
        this.request = request;
        this.hostId = hostId;
    }
}
