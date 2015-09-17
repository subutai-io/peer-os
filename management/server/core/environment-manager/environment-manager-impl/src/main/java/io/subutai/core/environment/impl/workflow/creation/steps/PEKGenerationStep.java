package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.peer.api.LocalPeer;


/**
 * PEK generation step
 */
public class PEKGenerationStep
{
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final LocalPeer localPeer;


    public PEKGenerationStep( final Topology topology, final EnvironmentImpl environment, final LocalPeer localPeer )
    {
        this.topology = topology;
        this.environment = environment;
        this.localPeer = localPeer;
    }


    public Map<Peer, String> execute() throws PeerException
    {
        Set<Peer> peers = Sets.newHashSet(topology.getAllPeers());
        peers.add( localPeer );

        Map<Peer, String> peerPekPubKeys = Maps.newHashMap();

        for ( final Peer peer : peers )
        {
            peerPekPubKeys.put( peer,
                    peer.createEnvironmentKeyPair( String.format( "%s-%s", peer.getId(), environment.getId() ) ) );
        }

        return peerPekPubKeys;
    }
}
