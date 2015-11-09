package io.subutai.core.localpeer.impl.command;


import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandRequest;
import io.subutai.common.command.CommandResponse;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultImpl;
import io.subutai.common.command.Response;
import io.subutai.common.command.ResponseImpl;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RequestListener;
import io.subutai.common.peer.Timeouts;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.peer.RecipientType;
import io.subutai.core.peer.api.PeerManager;


public class CommandRequestListener extends RequestListener
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandRequestListener.class.getName() );


    protected PeerManager getPeerManager() throws PeerException
    {
        try
        {
            return ServiceLocator.getServiceNoCache( PeerManager.class );
        }
        catch ( NamingException e )
        {
            throw new PeerException( e );
        }
    }


    public CommandRequestListener()
    {
        super( RecipientType.COMMAND_REQUEST.name() );
    }


    @Override
    public Object onRequest( final Payload payload ) throws PeerException
    {
        final CommandRequest commandRequest = payload.getMessage( CommandRequest.class );

        if ( commandRequest != null )
        {
            try
            {
                Peer sourcePeer = getPeerManager().getPeer( payload.getSourcePeerId() );
                LocalPeer localPeer = getPeerManager().getLocalPeer();
                Host host = localPeer.bindHost( commandRequest.getHostId() );
                localPeer.executeAsync( commandRequest.getRequestBuilder(), host,
                        new CommandRequestCallback( commandRequest, sourcePeer ) );
            }
            catch ( CommandException e )
            {
                LOG.error( "Error in onMessage", e );
            }
        }
        else
        {
            LOG.warn( "Null request" );
        }

        return null;
    }


    protected static class CommandRequestCallback implements CommandCallback
    {
        private final CommandRequest commandRequest;
        private final Peer sourcePeer;


        public CommandRequestCallback( final CommandRequest commandRequest, final Peer sourcePeer )
        {
            this.commandRequest = commandRequest;
            this.sourcePeer = sourcePeer;
        }


        @Override
        public void onResponse( final Response response, final CommandResult commandResult )
        {
            try
            {
                sourcePeer.sendRequest(
                        new CommandResponse( commandRequest.getRequestId(), new ResponseImpl( response ),
                                new CommandResultImpl( commandResult ) ), RecipientType.COMMAND_RESPONSE.name(),
                        Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT, null );
            }
            catch ( PeerException e )
            {
                LOG.error( "Error in onMessage", e );
            }
        }
    }
}
