package org.safehaus.subutai.core.peer.command.dispatcher.rest;


import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandType;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService
{

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );
    private PeerManager peerManager;
    private PeerCommandDispatcher peerCommandDispatcher;


    public RestServiceImpl()
    {
    }


    public void setPeerCommandDispatcher( final PeerCommandDispatcher peerCommandDispatcher )
    {
        this.peerCommandDispatcher = peerCommandDispatcher;
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


    @Override
    public String createContainers( final String createContainersMsg )
    {
        CloneContainersMessage ccm = GSON.fromJson( createContainersMsg, CloneContainersMessage.class );
        LOG.info( "Message to clone container received for environment: " + ccm.getEnvId() );
        peerManager.createContainers( ccm );
        return JsonUtil.toJson( ccm.getResult() );
    }


    @Override
    public String getCreateContainersMsgJsonFormat()
    {
        CloneContainersMessage ccm = new CloneContainersMessage();
        ccm.setStrategy( "ROUND_ROBIN" );
        ccm.setEnvId( UUID.randomUUID() );
        ccm.setNumberOfNodes( 2 );
        ccm.setPeerId( UUID.randomUUID() );
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

        PeerCommandType type = PeerCommandType.valueOf( commandType );
        Class clazz = getMessageClass( type );
        PeerCommandMessage commandMessage = ( PeerCommandMessage ) JsonUtil.fromJson( command, clazz );
        LOG.debug( String.format( "Before =============[%s]", commandMessage,
                commandMessage != null ? commandMessage.toString() : "NULL" ) );
        peerCommandDispatcher.invoke( commandMessage );
        LOG.debug( String.format( "After =============[%s]", commandMessage,
                commandMessage != null ? commandMessage.toString() : "NULL" ) );

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
            default:
                return PeerCommandMessage.class;
        }
    }


    @Override
    public Response ping()
    {
        return Response.ok().build();
    }


    private Peer getSamplePeer()
    {
        Peer peer = new Peer();
        peer.setName( "Peer name" );
        peer.setIp( "10.10.10.10" );
        peer.setId( UUID.randomUUID() );
        return peer;
    }
}