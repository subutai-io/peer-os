package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.peer.api.PeerManager;


public class PEKGenerationStep
{
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final PeerManager peerManager;


    public PEKGenerationStep( final Topology topology, final EnvironmentImpl environment,
                              final PeerManager peerManager )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
    }


    public Map<Peer, String> execute() throws PeerException
    {
        Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );

        //remove already existing peers
        peers.removeAll( environment.getPeers() );
        peers.remove( peerManager.getLocalPeer() );

        Map<Peer, String> peerPekPubKeys = Maps.newHashMap();

        for ( final Peer peer : peers )
        {
            peerPekPubKeys.put( peer, peer.createPeerEnvironmentKeyPair( environment.getEnvironmentId() ).getKey() );
        }

        return peerPekPubKeys;
    }
}
