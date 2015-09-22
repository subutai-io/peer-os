package io.subutai.core.environment.impl.workflow.destruction.steps;


import java.util.HashMap;
import java.util.Map;

import io.subutai.common.environment.PeerConf;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.peer.api.LocalPeer;


public class CleanupN2NStep
{
    private final EnvironmentImpl environment;
    private final LocalPeer localPeer;


    public CleanupN2NStep( final EnvironmentImpl environment, final LocalPeer localPeer )
    {
        this.environment = environment;
        this.localPeer = localPeer;
    }


    public void execute() throws PeerException
    {

        Map<String, N2NConfig> n2nConfigs = new HashMap<>();

        for ( PeerConf p : environment.getPeerConfs() )
        {
            n2nConfigs.put( p.getPeerId(), new N2NConfig( p.getTunnelAddress(), environment.getTunnelInterfaceName(),
                    environment.getTunnelCommunityName() ) );
        }

        for ( Peer peer : environment.getPeers() )
        {
            peer.removeN2NConnection( n2nConfigs.get( peer.getId() ) );
        }
    }
}
