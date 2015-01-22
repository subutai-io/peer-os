package org.safehaus.subutai.core.network.impl.remote;


import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.network.api.ContainerInfo;
import org.safehaus.subutai.core.network.api.N2NConnection;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.network.api.Tunnel;
import org.safehaus.subutai.core.network.impl.ContainerInfoImpl;
import org.safehaus.subutai.core.network.impl.N2NConnectionImpl;
import org.safehaus.subutai.core.network.impl.TunnelImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.google.gson.reflect.TypeToken;


class RemoteNetworkRestClient
{

    private static final Logger LOG = LoggerFactory.getLogger( RemoteNetworkRestClient.class );
    private static final long DEFAULT_CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis( 30 );
    private static final long DEFAULT_RECEIVE_TIMEOUT = TimeUnit.SECONDS.toMillis( 60 * 2 );

    private final String baseUrl;
    private long connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;


    public RemoteNetworkRestClient( String ip, int port )
    {
        baseUrl = String.format( "http://%s:%s/cxf/network", ip, port );
    }


    public void setConnectionTimeout( long connectionTimeout )
    {
        this.connectionTimeout = connectionTimeout;
    }


    public void setReceiveTimeout( long receiveTimeout )
    {
        this.receiveTimeout = receiveTimeout;
    }


    public List<N2NConnection> listN2NConnections()
    {
        String path = "n2n";
        WebClient client = createWebClient();
        Response resp = client.path( path ).get();

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            LOG.error( "Failed to list N2N connections on remote: {}", resp.getEntity() );
            return Collections.emptyList();
        }

        TypeToken<List<N2NConnectionImpl>> type = new TypeToken<List<N2NConnectionImpl>>()
        {
        };
        return JsonUtil.fromJson( resp.getEntity().toString(), type.getType() );
    }


    public void setupN2NConnection( N2NConnection n2n, String keyFilePath ) throws NetworkManagerException
    {
        String path = "n2n";
        Form form = new Form().set( "n2n", JsonUtil.to( n2n ) ).set( "keyFile", keyFilePath );

        WebClient client = createWebClient();
        Response resp = client.path( path ).form( form );

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            throw new NetworkManagerException( "Failed to setup N2N connection on remote: " + resp.getEntity() );
        }
    }


    public void removeN2NConnection( String interfaceName, String communityName ) throws NetworkManagerException
    {
        String path = String.format( "n2n/%s/%s", interfaceName, communityName );

        WebClient client = createWebClient();
        Response resp = client.path( path ).delete();

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            throw new NetworkManagerException( "Failed to delete N2N connection" );
        }
    }


    public List<Tunnel> listTunnels()
    {
        String path = "tunnel";

        WebClient client = createWebClient();
        Response resp = client.path( path ).get();

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            LOG.error( "Failed to list tunnels on remote: {}", resp.getEntity() );
            return Collections.emptyList();
        }

        TypeToken<List<TunnelImpl>> type = new TypeToken<List<TunnelImpl>>()
        {
        };
        return JsonUtil.GSON.fromJson( resp.getEntity().toString(), type.getType() );
    }


    public void setupTunnel( Tunnel tunnel, String type ) throws NetworkManagerException
    {
        String path = "tunnel";
        Form form = new Form().set( "tunnel", JsonUtil.to( tunnel ) ).set( "type", type );

        WebClient client = createWebClient();
        Response resp = client.path( path ).form( form );

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            throw new NetworkManagerException( "Failed to setup tunnel on remote: " + resp.getEntity() );
        }
    }


    public void removeTunnel( String tunnelName ) throws NetworkManagerException
    {
        String path = "tunnel/" + tunnelName;
        WebClient client = createWebClient();
        Response resp = client.path( path ).delete();

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            throw new NetworkManagerException( "Failed to remove tunnel" );
        }
    }


    public ContainerInfo getContainerInfo( String containerName ) throws NetworkManagerException
    {
        String path = String.format( "containers/%s/ip", containerName );

        WebClient client = createWebClient();
        Response resp = client.path( path ).get();

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            throw new NetworkManagerException( "Failed to get container IP" );
        }
        return JsonUtil.fromJson( resp.getEntity().toString(), ContainerInfoImpl.class );
    }


    public void setContainerIp( String containerName, String ip, int netMask, int vlanId ) throws NetworkManagerException
    {
        String path = String.format( "containers/%s/ip", containerName );
        Form form = new Form().set( "ip", ip ).set( "netMask", netMask ).set( "vLanId", vlanId );

        WebClient client = createWebClient();
        Response resp = client.path( path ).form( form );

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            throw new NetworkManagerException( "Failed to set container IP on remote: " + resp.getEntity() );
        }
    }


    public void removeContainerIp( String containerName ) throws NetworkManagerException
    {
        String path = String.format( "containers/%s/ip", containerName );

        WebClient client = createWebClient();
        Response resp = client.path( path ).delete();

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            throw new NetworkManagerException( "Failed to remove container IP" );
        }
    }


    public void setupGateway( String gatewayIp, int vlanId ) throws NetworkManagerException
    {
        String path = "gateway";
        Form form = new Form().set( "gatewayIp", gatewayIp ).set( "vLanId", vlanId );

        WebClient client = createWebClient();
        Response resp = client.path( path ).form( form );

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            throw new NetworkManagerException( "Failed to setup gateway on remote: " + resp.getEntity() );
        }
    }


    public void removeGateway( int vlanId ) throws NetworkManagerException
    {
        String path = "gateway/" + vlanId;

        WebClient client = createWebClient();
        Response resp = client.path( path ).delete();

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            throw new NetworkManagerException( "Failed to setup gateway on remote: " + resp.getEntity() );
        }
    }


    public void setupGatewayOnContainer( String containerName, String gatewayIp, String interfaceName ) throws NetworkManagerException
    {
        String path = String.format( "containers/%s/gateway", containerName );
        Form form = new Form().set( "gatewayIp", gatewayIp ).set( "interfaceName", interfaceName );

        WebClient client = createWebClient();
        Response resp = client.path( path ).form( form );

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            throw new NetworkManagerException( "Failed to setup gateway on container on remote: " + resp.getEntity() );
        }
    }


    public void removeGatewayOnContainer( String containerName ) throws NetworkManagerException
    {
        String path = String.format( "containers/%s/gateway", containerName );

        WebClient client = createWebClient();
        Response resp = client.path( path ).delete();

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            throw new NetworkManagerException( "Failed to setup gateway on container on remote: " + resp.getEntity() );
        }
    }


    public void setupVniVlanMapping( String tunnelName, int vni, int vLanId ) throws NetworkManagerException
    {
        String path = "mapping";
        Form form = new Form().set( "tunnelName", tunnelName ).set( "vni", vni ).set( "vLanId", vLanId );

        WebClient client = createWebClient();
        Response resp = client.path( path ).form( form );

        if ( resp.getStatusInfo() != Response.Status.OK )
        {
            throw new NetworkManagerException( "Failed to setup vni-vlan mapping on remote: " + resp.getEntity() );
        }
    }


    public boolean removeVniVlanMapping( String tunnelName, int vni, int vLanId )
    {
        throw new UnsupportedOperationException( "add impl if needed" );
    }


    private WebClient createWebClient()
    {
        WebClient client = WebClient.create( baseUrl );
        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( connectionTimeout );
        httpClientPolicy.setReceiveTimeout( receiveTimeout );

        httpConduit.setClient( httpClientPolicy );
        return client;
    }

}

