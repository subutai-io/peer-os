package io.subutai.core.environment.impl.workflow.destruction.steps;


import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Sets;

import io.subutai.common.environment.PeerConf;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class CleanupP2PStep
{
    private final EnvironmentImpl environment;


    public CleanupP2PStep( final EnvironmentImpl environment )
    {
        this.environment = environment;
    }


    public void execute() throws PeerException
    {
        Set<String> peerIps = Sets.newHashSet();
        for ( PeerConf peerConf : environment.getPeerConfs() )
        {
            peerIps.add( peerConf.getTunnelAddress() );
        }

        //todo run these in a thread
        for ( Peer peer : environment.getPeers() )
        {
            peer.removeP2PConnection( environment.getEnvironmentId() );
            peer.removeTunnels( peerIps );
        }
    }


    private class RemoveTunnelTask implements Callable<Boolean>
    {
        private final Peer peer;
        private final Set<String> tunnels;


        public RemoveTunnelTask( final Peer peer, final Set<String> tunnels )
        {
            this.peer = peer;
            this.tunnels = tunnels;
        }


        @Override
        public Boolean call() throws Exception
        {
            peer.removeTunnels( tunnels );

            return true;
        }
    }
}
