package org.safehaus.subutai.core.peer.impl.command;


import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.impl.RecipientType;
import org.safehaus.subutai.core.peer.impl.Timeouts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandRequestListener extends RequestListener
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandRequestListener.class.getName() );

    private LocalPeer localPeer;
    private PeerManager peerManager;


    public CommandRequestListener( final LocalPeer localPeer, final PeerManager peerManager )
    {
        super( RecipientType.COMMAND_REQUEST.name() );
        this.localPeer = localPeer;
        this.peerManager = peerManager;
    }


    @Override
    public Object onRequest( final Payload payload ) throws PeerException
    {
        final CommandRequest commandRequest = payload.getMessage( CommandRequest.class );

        if ( commandRequest != null )
        {
            try
            {
                final Peer sourcePeer = peerManager.getPeer( payload.getSourcePeerId() );
                Host host = localPeer.bindHost( commandRequest.getHostId() );

                localPeer.executeAsync( commandRequest.getRequestBuilder(), host, new CommandCallback()
                {
                    @Override
                    public void onResponse( final Response response, final CommandResult commandResult )
                    {
                        try
                        {
                            sourcePeer.sendRequest(
                                    new CommandResponse( commandRequest.getRequestId(), new ResponseImpl( response ),
                                            new CommandResultImpl( commandResult ) ),
                                    RecipientType.COMMAND_RESPONSE.name(), Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT );
                        }
                        catch ( PeerException e )
                        {
                            LOG.error( "Error in onMessage", e );
                        }
                    }
                } );
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
}
