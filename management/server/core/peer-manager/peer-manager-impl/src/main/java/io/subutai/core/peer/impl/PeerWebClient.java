package io.subutai.core.peer.impl;


import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.Gateways;
import io.subutai.common.network.Vni;
import io.subutai.common.network.Vnis;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.ControlNetworkConfig;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.protocol.PingDistances;
import io.subutai.common.resource.HistoricalMetrics;
import io.subutai.common.resource.PeerResources;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.WebClientBuilder;
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


    public PeerWebClient( final PeerInfo peerInfo, final Object provider )
    {
        this.peerInfo = peerInfo;
        this.provider = provider;
    }


    public PeerInfo getInfo() throws PeerException
    {
        String path = "/info";

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider, 4000, 7000, 1 );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            return client.get( PeerInfo.class );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error getting peer info", e );
        }
    }


    void startContainer( ContainerId containerId ) throws PeerException
    {

        String path = "/container/start";
        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( containerId );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error starting container", e );
        }
    }


    void stopContainer( ContainerId containerId ) throws PeerException
    {
        String path = "/container/stop";
        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( containerId );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error stopping container", e );
        }
    }


    void destroyContainer( ContainerId containerId ) throws PeerException
    {
        String path = "/container/destroy";
        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( containerId );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error destroying container", e );
        }
    }


    public ContainerHostState getState( final ContainerId containerId ) throws PeerException
    {
        String path = "/container/state";
        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.post( containerId, ContainerHostState.class );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error getting container state ", e );
        }
    }


    public ProcessResourceUsage getProcessResourceUsage( final ContainerId containerId, int pid ) throws PeerException
    {
        String path = String.format( "/container/%s/usage/%d", containerId.getId(), pid );
        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( ProcessResourceUsage.class );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on obtaining process resource usage", e );
        }
    }


    public PublicKeyContainer createEnvironmentKeyPair( EnvironmentId environmentId ) throws PeerException
    {
        String path = "/pek";


        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.post( environmentId, PublicKeyContainer.class );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on creating peer environment key", e );
        }
    }


    public void updateEnvironmentPubKey( PublicKeyContainer publicKeyContainer ) throws PeerException
    {
        String path = "/pek";


        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.put( publicKeyContainer );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on updating  peer environment key", e );
        }
    }


    public void removePeerEnvironmentKeyPair( final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId );

        String path = String.format( "/pek/%s", environmentId.getId() );


        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.delete();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on removing peer environment key", e );
        }
    }


    public HostInterfaces getInterfaces() throws PeerException
    {
        String path = "/interfaces";

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( HostInterfaces.class );
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


        String path = "/p2presetkey";

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( new P2PCredentials( p2pHash, newSecretKey, ttlSeconds ) );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error resetting P2P secret key", e );
        }
    }


    public String setupP2PConnection( final P2PConfig config ) throws PeerException
    {
        LOG.debug( String.format( "Adding remote peer to p2p swarm: %s %s", config.getHash(), config.getAddress() ) );

        String path = "/p2ptunnel";

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.TEXT_PLAIN );

        try
        {
            Response response = client.post( config );
            return response.readEntity( String.class );
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
            client.post( config );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error setting up initial P2P connection", e );
        }
    }


    public void removeP2PConnection( final String p2pHash ) throws PeerException
    {
        LOG.debug( String.format( "Removing remote peer from p2p swarm: %s", p2pHash ) );

        String path = String.format( "/p2ptunnel/%s", p2pHash );

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            client.delete();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error removing p2p connection", e );
        }
    }


    public void cleanupEnvironment( final EnvironmentId environmentId ) throws PeerException
    {
        LOG.debug( String.format( "Cleaning up environment: %s", environmentId.getId() ) );

        String path = String.format( "/cleanup/%s", environmentId.getId() );

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            client.delete();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error cleaning up environment.", e );
        }
    }


    public ResourceHostMetrics getResourceHostMetrics() throws PeerException
    {
        String path = "/resources";

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            return client.get( ResourceHostMetrics.class );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error getting rh metrics", e );
        }
    }


    public Gateways getGateways() throws PeerException
    {
        String path = "/gateways";

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            return client.get( Gateways.class );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error getting gateways", e );
        }
    }


    public Vnis getReservedVnis() throws PeerException
    {
        String path = "/vni";

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            return client.get( Vnis.class );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( String.format( "Error obtaining reserved VNIs from peer %s", peerInfo ), e );
        }
    }


    public Vni reserveVni( final Vni vni ) throws PeerException
    {
        String path = "/vni";

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );
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


    public void alert( final AlertEvent alert ) throws PeerException
    {
        String path = "/alert";

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
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
            final DateTimeParam startParam = new DateTimeParam( startTime );
            final DateTimeParam endParam = new DateTimeParam( endTime );
            String path = String.format( "/hmetrics/%s/%s/%s/%s/%s", hostName, startParam.getDateString(),
                    startParam.getTimeString(), endParam.getDateString(), endParam.getTimeString() );

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            return client.get( HistoricalMetrics.class );
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
            String path = String.format( "/limits/%s", peerId );

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider, 4000, 7000, 1 );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            return client.get( PeerResources.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on retrieving peer limits.", e );
        }
    }


    public ControlNetworkConfig getControlNetworkConfig( final String localPeerId ) throws PeerException
    {
        try
        {
            String path = String.format( "/control/config/%s", localPeerId );

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider, 4000, 7000, 1 );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            return client.get( ControlNetworkConfig.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on retrieving control network config.", e );
        }
    }


    public boolean updateControlNetworkConfig( final ControlNetworkConfig config ) throws PeerException
    {
        Preconditions.checkNotNull( config );
        Preconditions.checkNotNull( config.getAddress() );
        Preconditions.checkNotNull( config.getP2pHash() );
        Preconditions.checkNotNull( config.getPeerId() );
        Preconditions.checkNotNull( config.getSecretKey() );
        Preconditions.checkArgument( config.getSecretKeyTtlSec() > 0 );
        try
        {
            String path = "/control/update";

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider, 4000, 7000, 1 );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            return client.put( config, Boolean.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on updating control network config.", e );
        }
    }


    public PingDistances getP2PSwarmDistances( final String p2pHash, final Integer maxAddress ) throws PeerException
    {
        Preconditions.checkNotNull( p2pHash );
        Preconditions.checkNotNull( maxAddress );
        try
        {
            String path = String.format( "/control/%s/%d/distance", p2pHash, maxAddress );

            WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider, 4000, 7000, 1 );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            return client.get( PingDistances.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on getting p2p swarm distances.", e );
        }
    }


    public int setupTunnels( final Map<String, String> peerIps, final String environmentId )
    {
        Preconditions.checkNotNull( peerIps );
        Preconditions.checkNotNull( environmentId );
        String path = String.format( "/tunnels/%s", environmentId );

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider, 500, 7000, 1 );
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.TEXT_PLAIN );
        return client.post( peerIps, Integer.class );
    }


    public void addPeerEnvironmentPubKey( final String keyId, final String pubKeyRing )
    {
        Preconditions.checkNotNull( keyId );
        Preconditions.checkNotNull( pubKeyRing );
        String path = String.format( "/pek/add/%s", keyId );

        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider, 500, 7000, 1 );
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        client.post( pubKeyRing );
    }


    public HostId getResourceHosIdByContainerId( final ContainerId containerId ) throws PeerException
    {
        String path = String.format( "/container/%s/rhId", containerId.getId() );
        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( HostId.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining resource host id by container id", e );
        }
    }


    public Set<ContainerHostInfo> getEnvironmentContainers( final EnvironmentId environmentId ) throws PeerException
    {
        String path = String.format( "/containers/%s", environmentId.getId() );
        WebClient client = WebClientBuilder.buildPeerWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return new HashSet<>( client.getCollection( ContainerHostInfo.class ) );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining environment containers.", e );
        }
    }
}
