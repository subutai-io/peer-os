package io.subutai.core.peer.impl;


import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.environment.Containers;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.NetworkResourceImpl;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.PingDistances;
import io.subutai.common.resource.HistoricalMetrics;
import io.subutai.common.resource.PeerResources;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.WebClientBuilder;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.util.DateTimeParam;


/**
 * Peer REST client
 *
 * TODO throw exception if http code is not 2XX
 */
public class PeerWebClient
{
    private static final Logger LOG = LoggerFactory.getLogger( PeerWebClient.class );

    private Object provider;
    private PeerInfo peerInfo;
    private RemotePeerImpl remotePeer;


    public PeerWebClient( final Object provider, final PeerInfo peerInfo, final RemotePeerImpl remotePeer )
    {
        this.provider = provider;
        this.peerInfo = peerInfo;
        this.remotePeer = remotePeer;
    }


    public PeerInfo getInfo() throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = "/info";

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider, 3000, 15000, 1 );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );

            final Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( PeerInfo.class );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error getting peer info", e );
        }
    }


    public ProcessResourceUsage getProcessResourceUsage( final ContainerId containerId, int pid ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/container/%s/usage/%d", containerId.getId(), pid );
            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( ProcessResourceUsage.class );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on obtaining process resource usage", e );
        }
    }


    public PublicKeyContainer createEnvironmentKeyPair( final RelationLinkDto envLink ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = "/pek";

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );

            final Response response = client.post( envLink );
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( PublicKeyContainer.class );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on creating peer environment key", e );
        }
    }


    public void updateEnvironmentPubKey( PublicKeyContainer publicKeyContainer ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = "/pek";

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.put( publicKeyContainer );
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on updating  peer environment key", e );
        }
    }


    public HostInterfaces getInterfaces() throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = "/interfaces";

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( HostInterfaces.class );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error getting interfaces", e );
        }
    }


    public void resetP2PSecretKey( final String p2pHash, final String newSecretKey, final long ttlSeconds )
            throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ), "Invalid P2P hash" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newSecretKey ), "Invalid secret key" );
        Preconditions.checkArgument( ttlSeconds > 0, "Invalid time-to-live" );
        try
        {
            remotePeer.checkRelation();
            String path = "/p2presetkey";

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.post( new P2PCredentials( p2pHash, newSecretKey, ttlSeconds ) );
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error resetting P2P secret key", e );
        }
    }


    public String getP2PIP( final String resourceHostId, final String swarmHash ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( resourceHostId ), "Invalid resource host id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( swarmHash ), "Invalid p2p swarm hash" );

        String path = String.format( "/p2pip/%s/%s", resourceHostId, swarmHash );

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.accept( MediaType.TEXT_PLAIN );

        try
        {
            final Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( String.class );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error getting P2P IP", e );
        }
    }


    public P2PConnections setupP2PConnection( final P2PConfig config ) throws PeerException
    {
        LOG.debug( String.format( "Adding remote peer to p2p swarm: %s %s", config.getHash(), config.getAddress() ) );
        try
        {
            remotePeer.checkRelation();

            String path = "/p2ptunnel";

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );

            final Response response = client.post( config );
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( P2PConnections.class );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error setting up P2P connection", e );
        }
    }


    public void setupInitialP2PConnection( final P2PConfig config ) throws PeerException
    {
        LOG.debug( String.format( "Setting up initial p2p connection in swarm: %s %s", config.getHash(),
                config.getAddress() ) );

        String path = "/p2pinitial";

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );

        try
        {
            final Response response = client.post( config );
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error setting up initial P2P connection", e );
        }
    }


    public void cleanupEnvironment( final EnvironmentId environmentId ) throws PeerException
    {
        LOG.debug( String.format( "Cleaning up environment: %s", environmentId.getId() ) );
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/cleanup/%s", environmentId.getId() );

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            Response response = client.delete();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error cleaning up environment.", e );
        }
    }


    public ResourceHostMetrics getResourceHostMetrics() throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = "/resources";

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( ResourceHostMetrics.class );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error getting rh metrics", e );
        }
    }


    public void alert( final AlertEvent alert ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = "/alert";

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            Response response = client.post( alert );
            if ( Response.Status.ACCEPTED.getStatusCode() != response.getStatus() )
            {
                throw new PeerException( "Alert not accepted." );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on alert", e );
        }
    }


    public HistoricalMetrics getHistoricalMetrics( final String hostName, final Date startTime, final Date endTime )
            throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            final DateTimeParam startParam = new DateTimeParam( startTime );
            final DateTimeParam endParam = new DateTimeParam( endTime );
            String path = String.format( "/hmetrics/%s/%s/%s/%s/%s", hostName, startParam.getDateString(),
                    startParam.getTimeString(), endParam.getDateString(), endParam.getTimeString() );

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );

            final Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( HistoricalMetrics.class );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on retrieving historical metrics from remote peer", e );
        }
    }


    public PeerResources getResourceLimits( final String peerId ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/limits/%s", peerId );

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider, 3000, 15000, 1 );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( PeerResources.class );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on retrieving peer limits.", e );
        }
    }


    public PingDistances getP2PSwarmDistances( final String p2pHash, final Integer maxAddress ) throws PeerException
    {
        Preconditions.checkNotNull( p2pHash );
        Preconditions.checkNotNull( maxAddress );
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/control/%s/%d/distance", p2pHash, maxAddress );

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider, 3000, 15000, 1 );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( PingDistances.class );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on getting p2p swarm distances.", e );
        }
    }


    public void setupTunnels( final P2pIps p2pIps, final String environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( p2pIps );
        Preconditions.checkNotNull( environmentId );

        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/tunnels/%s", environmentId );
            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider, 3000, 60000, 1 );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.TEXT_PLAIN );
            final Response response = client.post( p2pIps );
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( e );
        }
    }


    public void addPeerEnvironmentPubKey( final String keyId, final String pubKeyRing ) throws PeerException
    {
        Preconditions.checkNotNull( keyId );
        Preconditions.checkNotNull( pubKeyRing );

        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/pek/add/%s", keyId );

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider, 3000, 15000, 1 );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.post( pubKeyRing );
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( e );
        }
    }


    public HostId getResourceHosIdByContainerId( final ContainerId containerId ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/container/%s/rhId", containerId.getId() );
            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( HostId.class );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining resource host id by container id", e );
        }
    }


    public Containers getEnvironmentContainers( final EnvironmentId environmentId ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/containers/%s", environmentId.getId() );
            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( Containers.class );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining environment containers.", e );
        }
    }


    public UsedNetworkResources getUsedNetResources() throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = "/netresources";
            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

            client.accept( MediaType.APPLICATION_JSON );

            final Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( UsedNetworkResources.class );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining reserved network resources", e );
        }
    }


    public void reserveNetworkResource( final NetworkResourceImpl networkResource ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = "/netresources";

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );

            final Response response = client.post( networkResource );

            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception ex )
        {
            throw new PeerException( ex );
        }
    }
}
