package io.subutai.core.environment.impl.workflow.growing.steps;


import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class PEKGenerationStep
{
    private final Topology topology;
    private final EnvironmentImpl environment;


    public PEKGenerationStep( final Topology topology, final EnvironmentImpl environment )
    {
        this.topology = topology;
        this.environment = environment;
    }


    public Map<Peer, String> execute() throws PeerException
    {
        Set<Peer> peers = Sets.newHashSet( topology.getAllPeers() );

        //remove already existing peers
        peers.removeAll( environment.getPeers() );

        Map<Peer, String> peerPekPubKeys = Maps.newHashMap();

        for ( final Peer peer : peers )
        {
            peerPekPubKeys.put( peer,
                    peer.createEnvironmentKeyPair( String.format( "%s-%s", peer.getId(), environment.getId() ) ) );
        }

        return peerPekPubKeys;
    }
}
