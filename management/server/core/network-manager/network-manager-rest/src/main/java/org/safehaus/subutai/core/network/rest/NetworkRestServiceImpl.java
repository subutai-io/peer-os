package org.safehaus.subutai.core.network.rest;


import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.network.api.ContainerInfo;
import org.safehaus.subutai.core.network.api.N2NConnection;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.network.api.Tunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;


public class NetworkRestServiceImpl implements NetworkRestService
{

    private static final Logger LOG = LoggerFactory.getLogger( NetworkRestServiceImpl.class.getName() );

    private NetworkManager networkManager;


    public NetworkManager getNetworkManager()
    {
        return networkManager;
    }


    public void setNetworkManager( NetworkManager networkManager )
    {
        this.networkManager = networkManager;
    }


    @Override
    public Response listN2NConnections()
    {
        try
        {
            Set<N2NConnection> s = networkManager.listN2NConnections();
            return Response.ok( JsonUtil.to( s ), MediaType.APPLICATION_JSON ).build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to get N2N connections", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response setupN2NConnection( String n2n, String keyType, String keyFilePath )
    {
        try
        {
            N2NConnection n2nConn = JsonUtil.GSON.fromJson( n2n, N2NConnectionImpl.class );
            networkManager.setupN2NConnection( n2nConn.getSuperNodeIp(), n2nConn.getSuperNodePort(),
                    n2nConn.getInterfaceName(), n2nConn.getCommunityName(), n2nConn.getLocalIp(), keyType,
                    keyFilePath );
            return Response.ok().build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to setup N2N connection", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.error( "Failed to parse payload", ex );
            return Response.status( Response.Status.BAD_REQUEST ).entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response removeN2NConnection( String interfaceName, String communityName )
    {
        try
        {
            networkManager.removeN2NConnection( interfaceName, communityName );
            return Response.ok().build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to remove N2N connection", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response listTunnels()
    {
        try
        {
            Set<Tunnel> tunnels = networkManager.listTunnels();
            return Response.ok( JsonUtil.to( tunnels ), MediaType.APPLICATION_JSON ).build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to get tunnels", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response setupTunnel( int tunnelId, String tunnelIp )
    {
        try
        {
            networkManager.setupTunnel( tunnelId, tunnelIp );
            return Response.ok().build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to setup tunnel", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.error( "Failed to parse payload", ex );
            return Response.status( Response.Status.BAD_REQUEST ).entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response removeTunnel( int tunnelId )
    {
        try
        {
            networkManager.removeTunnel( tunnelId );
            return Response.ok().build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to remove tunnel", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response getContainerInfo( String containerName )
    {
        try
        {
            ContainerInfo ci = networkManager.getContainerIp( containerName );
            return Response.ok( JsonUtil.to( ci ) ).build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to get container IP", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response setContainerIp( String containerName, String ip, int netMask, int vLanId )
    {
        try
        {
            networkManager.setContainerIp( containerName, ip, netMask, vLanId );
            return Response.ok().build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to set container IP", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response removeContainerIp( String containerName )
    {
        try
        {
            networkManager.removeContainerIp( containerName );
            return Response.ok().build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to remove container IP", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response setupGateway( String gatewayIp, int vLanId )
    {
        try
        {
            networkManager.setupGateway( gatewayIp, vLanId );
            return Response.ok().build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to setup gateway", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response removeGateway( int vLanId )
    {
        try
        {
            networkManager.removeGateway( vLanId );
            return Response.ok().build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to remove gateway", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response setupGatewayOnContainer( String containerName, String gatewayIp, String interfaceName )
    {
        try
        {
            networkManager.setupGatewayOnContainer( containerName, gatewayIp, interfaceName );
            return Response.ok().build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to setup gateway on container", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response removeGatewayOnContainer( String containerName )
    {
        try
        {
            networkManager.removeGatewayOnContainer( containerName );
            return Response.ok().build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to remove gateway on container", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response setupVniVLanMapping( int tunnelId, int vni, int vLanId )
    {
        try
        {
            networkManager.setupVniVLanMapping( tunnelId, vni, vLanId );
            return Response.ok().build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to setup vni-vlan mapping", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }


    @Override
    public Response removeVniVLanMapping( int tunnelId, int vni, int vLanId )
    {
        try
        {
            networkManager.removeVniVLanMapping( tunnelId, vni, vLanId );
            return Response.ok().build();
        }
        catch ( NetworkManagerException ex )
        {
            LOG.error( "Failed to remove vni-vlan mapping", ex );
            return Response.serverError().entity( ex.getMessage() ).build();
        }
    }
}

