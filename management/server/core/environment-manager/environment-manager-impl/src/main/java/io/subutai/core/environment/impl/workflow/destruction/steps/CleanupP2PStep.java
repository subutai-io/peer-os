package io.subutai.core.environment.impl.workflow.destruction.steps;


import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class CleanupP2PStep
{
    private final EnvironmentImpl environment;
    //    private final LocalPeer localPeer;


    public CleanupP2PStep( final EnvironmentImpl environment/*, final LocalPeer localPeer*/ )
    {
        this.environment = environment;
        //        this.localPeer = localPeer;
    }


    public void execute() throws PeerException
    {

        //        Map<String, P2PConfig> p2pConfigs = new HashMap<>();
        //
        //        for ( PeerConf p : environment.getPeerConfs() )
        //        {
        //            p2pConfigs.put( p.getPeerId(), new P2PConfig( p.getTunnelAddress(), environment
        // .getTunnelInterfaceName(),
        //                    environment.getTunnelCommunityName() ) );
        //        }

        for ( Peer peer : environment.getPeers() )
        {
            peer.removeP2PConnection( environment.getEnvironmentId() );
        }
    }
}
