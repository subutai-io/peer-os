package io.subutai.core.peer.impl.command;


import java.util.Map;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.Response;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.api.RequestListener;
import io.subutai.core.peer.impl.RecipientType;
import io.subutai.core.peer.impl.Timeouts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;


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
                Peer sourcePeer = peerManager.getPeer( payload.getSourcePeerId() );
                Host host = localPeer.bindHost( commandRequest.getHostId() );

                localPeer.executeAsync( commandRequest.getRequestBuilder(), host,
                        new CommandRequestCallback( commandRequest, sourcePeer,localPeer ) );
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
        private Peer localPeer;


        public CommandRequestCallback( final CommandRequest commandRequest, final Peer sourcePeer, Peer localPeer)
        {
            this.commandRequest = commandRequest;
            this.sourcePeer = sourcePeer;
            this.localPeer = localPeer;
        }


        @Override
        public void onResponse( final Response response, final CommandResult commandResult)
        {
            try
            {
                //*********construct Secure Header ****************************
                Map<String, String> headers = Maps.newHashMap();
                String envId = commandRequest.getEnvironmentId();
                String envHeaderTarget = sourcePeer.getId()+"-"+envId;
                String envHeaderSource = localPeer.getId() +"-"+envId;

                headers.put( Common.HEADER_SPECIAL, "ENC");
                headers.put( Common.HEADER_ENV_ID_SOURCE,envHeaderSource );
                headers.put( Common.HEADER_ENV_ID_TARGET,envHeaderTarget );
                //**************************************************************************


                sourcePeer.sendRequest(
                        new CommandResponse( commandRequest.getRequestId(), new ResponseImpl( response ),
                                new CommandResultImpl( commandResult ) ), RecipientType.COMMAND_RESPONSE.name(),
                        Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT, headers );
            }
            catch ( PeerException e )
            {
                LOG.error( "Error in onMessage", e );
            }
        }
    }
}
