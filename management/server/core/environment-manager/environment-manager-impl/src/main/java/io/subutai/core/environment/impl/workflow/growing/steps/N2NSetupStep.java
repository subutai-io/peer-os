package io.subutai.core.environment.impl.workflow.growing.steps;


import java.util.Set;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class N2NSetupStep
{
    private final Topology topology;
    private final EnvironmentImpl environment;


    public N2NSetupStep( final Topology topology, final EnvironmentImpl environment )
    {
        this.topology = topology;
        this.environment = environment;
    }


    public Set<N2NConfig> execute()
    {

        Set<Peer> peers = Sets.newHashSet( topology.getAllPeers() );

        //remove already participating peers
        peers.removeAll( environment.getPeers() );

        //todo setup tunnels, create edge
        return null;
    }
}
