package io.subutai.core.peer.impl;


import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.SecuritySettings;
import io.subutai.common.util.RestUtil;


/**
 * Peer REST client
 */
public class PeerClient
{
    private Object provider;


    public PeerClient( final Object provider )
    {
        this.provider = provider;
    }


    void startContainer( String host, ContainerId containerId ) throws PeerException
    {

        String path = "/container/start";
        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );

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

        String path = "/container/stop";
        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );

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
