package org.safehaus.subutai.core.peer.command.dispatcher.impl;


import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.helpers.PeerCommand;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandDispatcher;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandException;


/**
 * Created by timur on 9/20/14.
 */
public class PeerCommandDispatcherImpl implements PeerCommandDispatcher
{
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

            try
            {
                result = peerManager.invoke( peerCommand );
            }
            catch ( PeerException pe )
            {
                throw new PeerCommandException( pe.getMessage() );
            }
        }
        else
        {

//            result =  remotePeerRestClient
            //TODO: remote peer invoke



        }
        return result;
    }
}
