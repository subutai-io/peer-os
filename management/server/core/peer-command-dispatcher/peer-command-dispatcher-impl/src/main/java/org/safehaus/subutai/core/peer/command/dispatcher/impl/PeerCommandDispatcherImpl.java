package org.safehaus.subutai.core.peer.command.dispatcher.impl;


import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandDispatcher;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by timur on 9/20/14.
 */
public class PeerCommandDispatcherImpl implements PeerCommandDispatcher
{
    private static final Logger LOG = LoggerFactory.getLogger( PeerCommandDispatcherImpl.class.getName() );
    private static final String port = "8181";
    private PeerManager peerManager;
    private RemotePeerRestClient remotePeerRestClient;


    public RemotePeerRestClient getRemotePeerRestClient()
    {
        return remotePeerRestClient;
    }


    public void setRemotePeerRestClient( final RemotePeerRestClient remotePeerRestClient )
    {
        this.remotePeerRestClient = remotePeerRestClient;
    }


    public void init()
    {
        // empty init
    }


    public void destroy()
    {
        // empty destroy
    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    public void invoke( PeerCommandMessage peerCommand ) throws PeerCommandException
    {
        try
        {
            if ( peerManager.getSiteId().equals( peerCommand.getPeerId() ) )
            {
                peerManager.invoke( peerCommand );
            }
            else
            {
                Peer peer = peerManager.getPeerByUUID( peerCommand.getPeerId() );
                remotePeerRestClient = new RemotePeerRestClient();
                remotePeerRestClient.invoke( peer.getIp(), port, peerCommand );
            }
        }
        catch ( RuntimeException e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerCommandException( "Error invoking Peer command" );
        }
    }


    @Override
    public void invoke( final PeerCommandMessage peerCommand, final long timeout ) throws PeerCommandException
    {
        try
        {
            Peer peer = peerManager.getPeerByUUID( peerCommand.getPeerId() );
            remotePeerRestClient = new RemotePeerRestClient( timeout );
            remotePeerRestClient.invoke( peer.getIp(), port, peerCommand );
        }
        catch ( RuntimeException e )
        {
            peerCommand.setSuccess( false );
            peerCommand.setExceptionMessage( e.toString() );
            LOG.error( e.getMessage(), e );
            throw new PeerCommandException( "Error invoking Peer command" );
        }
    }
}
