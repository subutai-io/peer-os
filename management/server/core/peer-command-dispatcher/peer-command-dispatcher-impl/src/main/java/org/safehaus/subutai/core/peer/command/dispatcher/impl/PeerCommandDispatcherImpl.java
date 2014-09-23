package org.safehaus.subutai.core.peer.command.dispatcher.impl;


import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.PeerCommand;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
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

    }


    public void destroy()
    {

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
    public boolean invoke( final PeerCommand peerCommand ) throws PeerCommandException
    {
        boolean result = false;
        if ( peerManager.getSiteId().equals( peerCommand.getMessage().getPeerId() ) )
        {

            LOG.info( "invoke called in PCD impl" );
            try
            {
                result = peerManager.invoke( peerCommand );
            }
            catch ( PeerException pe )
            {
                LOG.error( pe.getMessage() );
                throw new PeerCommandException( pe.getMessage() );
            }
        }
        else
        {
            Peer peer = peerManager.getPeerByUUID( peerCommand.getMessage().getPeerId() );
            CloneContainersMessage ccm = ( CloneContainersMessage ) peerCommand.getMessage();
            LOG.info( "Sending command to peer: " + peer.getIp() + ", Peer ID: " + peer.getId() );
            result = remotePeerRestClient.createRemoteContainers( peer.getIp(), "8181", ccm );
        }
        return result;
    }
}
