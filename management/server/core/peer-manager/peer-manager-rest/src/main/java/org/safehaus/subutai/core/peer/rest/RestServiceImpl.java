package org.safehaus.subutai.core.peer.rest;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.DefaultCommandMessage;
import org.safehaus.subutai.common.protocol.ExecuteCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandType;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.PeerStatus;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;
import org.safehaus.subutai.core.strategy.api.Criteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService
{

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );
    private PeerManager peerManager;


    public RestServiceImpl()
    {
    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    public Peer registerPeer( String config )
    {
        if ( config != null )
        {
            Peer peer = GSON.fromJson( config, Peer.class );
            peerManager.register( peer );
            return peer;
        }
        else
        {
            return null;
        }
    }


    //    @Override
    //    public String createContainers( final String createContainersMsg )
    //    {
    //        CloneContainersMessage ccm = GSON.fromJson( createContainersMsg, CloneContainersMessage.class );
    //        LOG.info( "Message to clone container received for environment: " + ccm.getEnvId() );
    ////        peerManager.createContainers( ccm );
    //
    //        return JsonUtil.toJson( ccm.getResult() );
    //
    //    }


    @Override
    public String getCreateContainersMsgJsonFormat()
    {
        CloneContainersMessage ccm = new CloneContainersMessage( UUIDUtil.generateTimeBasedUUID() );
        ccm.setEnvId( UUIDUtil.generateTimeBasedUUID() );
        ccm.setStrategy( "ROUND_ROBIN" );
        //        ccm.setEnvId( UUIDUtil.generateTimeBasedUUID() );
        ccm.setNumberOfNodes( 2 );
        //        ccm.setPeerId( UUIDUtil.generateTimeBasedUUID() );
        ccm.setTemplate( "master" );
        return GSON.toJson( ccm );
    }


    @Override
    public String getPeerJsonFormat()
    {
        Peer peer = getSamplePeer();
        return GSON.toJson( peer );
    }


    @Override
    public String getId()
    {

        UUID id = peerManager.getSiteId();
        return GSON.toJson( id );
    }


    @Override
    public Response processMessage( final String peerId, final String recipient, final String message )
    {
        try
        {
            String response = peerManager.processPeerMessage( peerId, recipient, message );

            return Response.ok( response ).build();
        }
        catch ( PeerMessageException e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response getConnectedAgents( final String environmentId )
    {
        try
        {
            String response = JsonUtil.toJson( peerManager.getConnectedAgents( environmentId ) );
            return Response.ok( response ).build();
        }
        catch ( JsonSyntaxException | PeerException e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response invoke( final String commandType, final String command )
    {

        LOG.info( String.format( "Received a new command: %s", commandType ) );
        PeerCommandType type = PeerCommandType.valueOf( commandType );
        Class clazz = getMessageClass( type );
        PeerCommandMessage commandMessage = ( PeerCommandMessage ) JsonUtil.fromJson( command, clazz );
        if ( commandMessage == null )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( "Could not restore command from JSON." )
                           .build();
        }

        LOG.debug( String.format( "Command before invoking PCD [%s]", commandMessage ) );
        peerManager.invoke( commandMessage );
        LOG.debug( String.format( "Command after invoking PCD [%s]", commandMessage ) );

        if ( commandMessage.isSuccess() )
        {
            return Response.ok().entity( commandMessage.toJson() ).build();
        }
        else
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( commandMessage.toJson() ).build();
        }
    }


    private Class getMessageClass( final PeerCommandType type )
    {
        switch ( type )
        {
            case CLONE:
                return CloneContainersMessage.class;
            case EXECUTE:
                return ExecuteCommandMessage.class;
            default:
                return DefaultCommandMessage.class;
        }
    }


    @Override
    public Response ping()
    {
        return Response.ok().build();
    }


    @Override
    public Response processRegisterRequest( String peer )
    {
        Peer p = GSON.fromJson( peer, Peer.class );
        peerManager.register( p );
        return Response.ok( GSON.toJson( p ) ).build();
    }


    @Override
    public Response unregisterPeer( String peerId )
    {
        UUID id = GSON.fromJson( peerId, UUID.class );
        boolean result = peerManager.unregister( id.toString() );
        if ( result )
        {
            return Response.ok( "Successfully unregistered peer: " + peerId ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response updatePeer( String peer )
    {
        Peer p = GSON.fromJson( peer, Peer.class );
        peerManager.update( p );
        return Response.ok( GSON.toJson( p ) ).build();
    }


    private Peer getSamplePeer()
    {
        String localIp = getLocalIp();
        Peer peer = new Peer();
        peer.setName( "Peer on " + localIp );
        peer.setIp( localIp );
        peer.setId( peerManager.getSiteId() );
        peer.setStatus( PeerStatus.REQUESTED );
        return peer;
    }


    private static String getLocalIp()
    {
        Enumeration<NetworkInterface> n;
        try
        {
            n = NetworkInterface.getNetworkInterfaces();
            for (; n.hasMoreElements(); )
            {
                NetworkInterface e = n.nextElement();

                Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements(); )
                {
                    InetAddress addr = a.nextElement();
                    if ( !addr.getHostAddress().startsWith( "10" ) && addr.isSiteLocalAddress() )
                    {
                        return ( addr.getHostName() );
                    }
                }
            }
        }
        catch ( SocketException e )
        {
            System.out.println( e.getMessage() );
        }


        return "127.0.0.1";
    }


    @Override
    public Response createContainers( final String ownerPeerId, final String environmentId, final String templates,
                                      final int quantity, final String strategyId, final String criteria )
    {

        //TODO: Implement criteria restoring
        List<Criteria> criteriaList = new ArrayList();
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            Set<ContainerHost> result = localPeer
                    .createContainers( UUID.fromString( ownerPeerId ), UUID.fromString( environmentId ),
                            ( List<Template> ) JsonUtil.fromJson( templates, new TypeToken<List<Template>>()
                            {
                            }.getType() ), quantity, strategyId, criteriaList );
            return Response.ok( JsonUtil.toJson( result ) ).build();
            //            return Response.ok().entity( result ).build();
        }
        catch ( ContainerCreateException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }
}