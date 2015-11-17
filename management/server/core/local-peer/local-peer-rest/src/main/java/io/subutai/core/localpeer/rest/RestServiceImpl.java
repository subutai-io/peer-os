package io.subutai.core.localpeer.rest;


import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.protocol.Template;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.RamQuota;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.common.peer.LocalPeer;


public class RestServiceImpl implements RestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RestServiceImpl.class );
    private LocalPeer localPeer;
    //    private Monitor monitor;
    protected JsonUtil jsonUtil = new JsonUtil();
    protected RestUtil restUtil = new RestUtil();


    public RestServiceImpl( final LocalPeer localPeer/*, Monitor monitor*/ )
    {
        this.localPeer = localPeer;
        //        this.monitor = monitor;
    }


    @Override
    public Response getLocalPeerInfo()
    {
        try
        {
            PeerInfo selfInfo = localPeer.getPeerInfo();
            return Response.ok( jsonUtil.to( selfInfo ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating local peer info #getLocalPeerInfo", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return localPeer.getPeerInfo();
    }


    @Override
    public Response getPeerPolicy( final String peerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

            PeerPolicy peerPolicy = localPeer.getPeerInfo().getPeerPolicy( peerId );
            if ( peerPolicy == null )
            {
                return Response.ok().build();
            }
            else
            {
                return Response.ok( jsonUtil.to( peerPolicy ) ).build();
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting peer policy #getPeerPolicy", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response updatePeer( String peer )
    {
        try
        {
            PeerInfo p = jsonUtil.from( peer, PeerInfo.class );
            p.setIp( getRequestIp() );
            p.setName( String.format( "Peer %s", p.getId() ) );
            //            localPeer.update( p );

            return Response.ok( jsonUtil.to( p ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating peer #updatePeer", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    protected String getRequestIp()
    {
        Message message = PhaseInterceptorChain.getCurrentMessage();
        HttpServletRequest request = ( HttpServletRequest ) message.get( AbstractHTTPDestination.HTTP_REQUEST );
        return request.getRemoteAddr();
    }


    @Override
    public Response getTemplate( final String templateName )
    {
        try
        {
            Template result = localPeer.getTemplate( templateName );
            return Response.ok( jsonUtil.to( result ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting template #getTemplate", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    //*********** Quota functions ***************


    @Override
    public Response getAvailableRamQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            return Response.ok( localPeer.getContainerHostById( containerId ).getAvailableRamQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting available ram quota #getAvailableRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getAvailableCpuQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            return Response.ok( localPeer.getContainerHostById( containerId ).getAvailableCpuQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting available cpu quota #getAvailableCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getAvailableDiskQuota( final String containerId, final String diskPartition )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            return Response.ok( jsonUtil.to( localPeer.getContainerHostById( containerId ).getAvailableDiskQuota(
                    jsonUtil.<DiskPartition>from( diskPartition, new TypeToken<DiskPartition>()
                    {}.getType() ) ) ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting available disk quota #getAvailableDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    //    @Override
    //    public ProcessResourceUsage getProcessResourceUsage( ContainerId containerId, int pid )
    //    {
    //        try
    //        {
    //            Preconditions.checkNotNull( containerId );
    //            Preconditions.checkArgument( pid > 0 );
    //
    //            return localPeer.getProcessResourceUsage( containerId, pid );
    //        }
    //        catch ( Exception e )
    //        {
    //            LOGGER.error( "Error getting processing resource usage #getProcessResourceUsage", e );
    //            throw new WebApplicationException(
    //                    Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build() );
    //        }
    //    }


    @Override
    public Response getRamQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            return Response.ok( localPeer.getContainerHostById( containerId ).getRamQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting ram quota #getRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getRamQuotaInfo( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            return Response.ok( localPeer.getContainerHostById( containerId ).getRamQuotaInfo() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting ram quota info #getRamQuotaInfo", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setRamQuota( final String containerId, final int ram )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            localPeer.getContainerHostById( containerId ).setRamQuota( ram );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting ram quota #setRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setRamQuota( final String containerId, final String ramQuota )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            localPeer.getContainerHostById( containerId )
                       .setRamQuota( jsonUtil.<RamQuota>from( ramQuota, new TypeToken<RamQuota>()
                       {}.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting ram quota #setRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            return Response.ok( localPeer.getContainerHostById( containerId ).getCpuQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting cpu quota #getCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuQuotaInfo( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            return Response.ok( localPeer.getContainerHostById( containerId ).getCpuQuotaInfo() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting cpu quota info #getCpuQuotaInfo", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuQuota( final String containerId, final int cpu )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            localPeer.getContainerHostById( containerId ).setCpuQuota( cpu );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting cpu quota #setCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuSet( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            return Response.ok( jsonUtil.to( localPeer.getContainerHostById( containerId ).getCpuSet() ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting cpu set #getCpuSet", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuSet( final String containerId, final String cpuSet )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            localPeer.getContainerHostById( containerId )
                       .setCpuSet( jsonUtil.<Set<Integer>>from( cpuSet, new TypeToken<Set<Integer>>()
                       {}.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting cpu set #setCpuSet", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getDiskQuota( final String containerId, final String diskPartition )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            return Response.ok( jsonUtil.to( localPeer.getContainerHostById( containerId ).getDiskQuota(
                    JsonUtil.<DiskPartition>fromJson( diskPartition, new TypeToken<DiskPartition>()
                    {}.getType() ) ) ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting disk quota #getDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setDiskQuota( final String containerId, final String diskQuota )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            localPeer.getContainerHostById( containerId )
                       .setDiskQuota( jsonUtil.<DiskQuota>from( diskQuota, new TypeToken<DiskQuota>()
                       {}.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting disk quota #setDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setDefaultGateway( final String containerId, final String gatewayIp )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            localPeer.getContainerHostById( containerId ).setDefaultGateway( gatewayIp );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting default gateway #setDefaultGateway", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getContainerHostInfoById( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            return Response.ok( jsonUtil.to( localPeer.getContainerHostInfoById( containerId ) ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting container host info by id #getContainerHostInfoById", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Collection<Vni> getReservedVnis()
    {
        try
        {
            return localPeer.getReservedVnis();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting reserved vnis #getReservedVnis", e );
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Collection<Gateway> getGateways()
    {
        try
        {
            return localPeer.getGateways();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting gateways #getGateways", e );
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Response createGateway( final Gateway gateway )
    {
        try
        {
            localPeer.createGateway( gateway );

            return Response.ok().status( Response.Status.CREATED ).build();
        }
        catch ( Exception ex )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( ex ).build();
        }
    }


    @Override
    public PublicKeyContainer createEnvironmentKeyPair( final EnvironmentId environmentId )
    {
        Preconditions.checkNotNull( environmentId );

        try
        {
            return localPeer.createEnvironmentKeyPair( environmentId );
        }
        catch ( Exception ex )
        {
            throw new WebApplicationException( ex );
        }
    }


    @Override
    public void removeEnvironmentKeyPair( final EnvironmentId environmentId )
    {
        try
        {
            localPeer.removeEnvironmentKeyPair( environmentId );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Vni reserveVni( final Vni vni )
    {
        try
        {
            return localPeer.reserveVni( vni );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error reserving vni #reserveVni", e );
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Response setupTunnels( final String peerIps, final String environmentId )
    {
        try
        {
            int vlan = localPeer
                    .setupTunnels( jsonUtil.<Map<String, String>>from( peerIps, new TypeToken<Map<String, String>>()
                    {}.getType() ), environmentId );

            return Response.ok( vlan ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting up tunnels #setupTunnels", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public HostInterfaces getNetworkInterfaces()
    {
        try
        {
            return localPeer.getInterfaces();
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public ResourceHostMetrics getResources()
    {
        try
        {
            return localPeer.getResourceHostMetrics();
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public void setupN2NConnection( final N2NConfig config )
    {
        try
        {
            localPeer.setupN2NConnection( config );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public void removeN2NConnection( final EnvironmentId environmentId )
    {
        try
        {
            localPeer.removeN2NConnection( environmentId );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public void cleanupNetwork( final EnvironmentId environmentId )
    {
        try
        {
            localPeer.cleanupEnvironmentNetworkSettings( environmentId );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }

    /* *************************************************************
     * Get Public key and save it in the local KeyServer
     *
     * TODO remove this method if not used
     */
    //    private String getRemotePeerPublicKey( String peerId, final String ip )
    //    {
    //        String baseUrl = String.format( "https://%s:%s/cxf", ip, ChannelSettings.SECURE_PORT_X1 );
    //        WebClient client = RestUtil.createTrustedWebClient( baseUrl, provider );
    //        client.type( MediaType.MULTIPART_FORM_DATA ).accept( MediaType.APPLICATION_JSON );
    //
    //        try
    //        {
    //            Response response = client.path( "security/keyman/getpublickeyring" ).query( "hostid", "" ).get();
    //
    //            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
    //            {
    //                // Get Remote peer Public Key and save in the local keystore
    //                String publicKeyring = response.readEntity( String.class );
    //
    //                securityManager.getKeyManager().savePublicKeyRing( peerId, ( short ) 3, publicKeyring );
    //            }
    //            return peerId;
    //        }
    //        catch ( Exception ex )
    //        {
    //            return "";
    //        }
    //    }
}