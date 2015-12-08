package io.subutai.core.peer.impl;


import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.resource.HistoricalMetrics;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.WebClientBuilder;
import io.subutai.common.util.DateTimeParam;


/**
 * Peer REST client
 */
public class PeerWebClient
{
    private static final Logger LOG = LoggerFactory.getLogger( PeerWebClient.class );

    private Object provider;
    private String host;


    public PeerWebClient( final String host, final Object provider )
    {
        this.host = host;
        this.provider = provider;
    }


    public PeerInfo getInfo() throws PeerException
    {
        String path = "/info";

        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider, 500, 3000, 1 );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            return client.get( PeerInfo.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error getting peer info", e );
        }
    }


    void startContainer( ContainerId containerId ) throws PeerException
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


    void stopContainer( ContainerId containerId ) throws PeerException
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


    void destroyContainer( ContainerId containerId ) throws PeerException
    {
        String path = "/container/destroy";
        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( containerId );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error destroying container", e );
        }
    }


    public ContainerHostState getState( final ContainerId containerId )
    {
        String path = "/container/state";
        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.post( containerId, ContainerHostState.class );
        }
        catch ( Exception e )
        {
            LOG.warn( "Error on getting container state: " + e.getMessage() );
            return ContainerHostState.UNKNOWN;
        }
    }


    public ProcessResourceUsage getProcessResourceUsage( final ContainerId containerId, int pid ) throws PeerException
    {
        String path = String.format( "/container/%s/usage/%d", containerId.getId(), pid );
        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( ProcessResourceUsage.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining process resource usage", e );
        }
    }


    public void cleanupEnvironmentNetworkSettings( final EnvironmentId environmentId ) throws PeerException
    {

        String path = String.format( "/network/%s", environmentId.getId() );

        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.delete();
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on cleaning up network settings", e );
        }
    }


    public PublicKeyContainer createEnvironmentKeyPair( EnvironmentId environmentId ) throws PeerException
    {
        String path = "/pek";


        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.post( environmentId, PublicKeyContainer.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on creating peer environment key", e );
        }
    }


    public void updateEnvironmentPubKey( PublicKeyContainer publicKeyContainer ) throws PeerException
    {
        String path = "/pek";


        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.put( publicKeyContainer );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on updating  peer environment key", e );
        }
    }


    public void removePeerEnvironmentKeyPair( final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId );

        String path = String.format( "/pek/%s", environmentId.getId() );


        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.delete();
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on removing peer environment key", e );
        }
    }


    public HostInterfaces getInterfaces() throws PeerException
    {
        String path = "/interfaces";

        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( HostInterfaces.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error getting interfaces", e );
        }
    }


    public void setupN2NConnection( final N2NConfig config ) throws PeerException
    {
        LOG.debug( String.format( "Adding remote peer to n2n community: %s:%d %s %s %s", config.getSuperNodeIp(),
                config.getN2NPort(), config.getInterfaceName(), config.getCommunityName(), config.getAddress() ) );

        String path = "/n2ntunnel";

        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            client.post( config );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error setting up n2n connection", e );
        }
    }


    public void removeN2NConnection( final EnvironmentId environmentId ) throws PeerException
    {
        LOG.debug( String.format( "Removing remote peer from n2n community: %s", environmentId.getId() ) );

        String path = String.format( "/n2ntunnel/%s", environmentId.getId() );

        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            client.delete();
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error removing n2n connection", e );
        }
    }


    public ResourceHostMetrics getResourceHostMetrics() throws PeerException
    {
        String path = "/resources";

        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            return client.get( ResourceHostMetrics.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error getting rh metrics", e );
        }
    }


    public Set<Gateway> getGateways() throws PeerException
    {
        String path = "/gateways";

        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            Collection response = client.getCollection( Gateway.class );
            return new HashSet<>( response );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error getting gateways", e );
        }
    }


    public Set<Vni> getReservedVnis() throws PeerException
    {
        String path = "/vni";

        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            final Collection response = client.getCollection( Vni.class );
            return new HashSet<>( response );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Error obtaining reserved VNIs from peer %s", host ), e );
        }
    }


    public Vni reserveVni( final Vni vni ) throws PeerException
    {
        String path = "/vni";

        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            return client.post( vni, Vni.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on reserving VNI", e );
        }
    }


    public void createGateway( final Gateway gateway ) throws PeerException
    {
        String path = "/gateways";

        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            client.post( gateway );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on creating gateway", e );
        }
    }


    public void alert( final AlertEvent alert ) throws PeerException
    {
        String path = "/alert";

        WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        Response response = client.post( alert );

        if ( Response.Status.ACCEPTED.getStatusCode() != response.getStatus() )
        {
            throw new PeerException( "Alert not accepted." );
        }
    }


    public HistoricalMetrics getHistoricalMetrics( final String hostName, final Date startTime, final Date endTime )
            throws PeerException
    {
        try
        {
            final DateTimeParam startParam = new DateTimeParam( startTime );
            final DateTimeParam endParam = new DateTimeParam( endTime );
            String path = String.format( "/hmetrics/%s/%s/%s/%s/%s", hostName, startParam.getDateString(),
                    startParam.getTimeString(), endParam.getDateString(), endParam.getTimeString() );

            WebClient client = WebClientBuilder.buildPeerWebClient( host, path, provider );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            return client.get( HistoricalMetrics.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on retrieving historical metrics from remote peer", e );
        }
    }
}
