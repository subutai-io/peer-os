package io.subutai.core.peer.impl;


import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.PeerException;


/**
 * Environment REST client
 */
public class EnvironmentClient
{
    private Object provider;


    public EnvironmentClient( final Object provider )
    {
        this.provider = provider;
    }


    void startContainer( String host, ContainerId containerId ) throws PeerException
    {
        String path = String.format( "/%s/container/start", containerId.getEnvironmentId().getId() );
        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( containerId );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error starting container", e );
        }
    }


    void stopContainer( String host, ContainerId containerId ) throws PeerException
    {
        String path = String.format( "/%s/container/stop", containerId.getEnvironmentId().getId() );
        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( containerId );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error stopping container", e );
        }
    }
}
