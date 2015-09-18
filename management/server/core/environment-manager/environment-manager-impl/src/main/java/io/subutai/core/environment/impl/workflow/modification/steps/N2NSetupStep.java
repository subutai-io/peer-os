package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.HashSet;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;

import io.subutai.common.environment.PeerConf;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.util.N2NUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;
import io.subutai.core.peer.api.LocalPeer;


public class N2NSetupStep
{
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final String supernode;
    private final int supernodePort;


    public N2NSetupStep( final Topology topology, final EnvironmentImpl environment, final String supernode,
                         final int supernodePort )
    {
        this.topology = topology;
        this.environment = environment;
        this.supernode = supernode;
        this.supernodePort = supernodePort;
    }


    public void execute() throws EnvironmentManagerException
    {
        Set<Peer> peers = Sets.newHashSet( topology.getAllPeers() );

        Set<String> peerIds = new HashSet<>();
        int maxIP = 1;
        for ( PeerConf pc : environment.getPeerConfs() )
        {
            N2NConfig n = pc.getN2NConfig();
            //temp fix
            peerIds.add( n.getPeerId() );
        }

        SubnetUtils.SubnetInfo info =
                new SubnetUtils( environment.getPeerConfs().iterator().next().getN2NConfig().getAddress(),
                        N2NUtil.N2N_SUBNET_MASK ).getInfo();

        String freeSubnet = info.getNetworkAddress();
        String interfaceName = N2NUtil.generateInterfaceName( freeSubnet );
        String communityName = N2NUtil.generateCommunityName( freeSubnet );
        String sharedKey = "secret";
        final String[] addresses = info.getAllAddresses();
        int counter = environment.getPeerConfs().size() + 1;
        for ( Peer peer : peers )
        {
            if ( !peerIds.contains( peer.getId() ) )
            {
                N2NConfig config = new N2NConfig( peer.getId(), supernode, supernodePort, interfaceName, communityName,
                        addresses[counter], sharedKey );
                try
                {
                    peer.setupN2NConnection( config );
                }
                catch ( PeerException e )
                {
                    throw new EnvironmentManagerException( "Could not create n2n connection on peer: " + peer.getId(),
                            e );
                }
                final PeerConf p = new PeerConfImpl();
                p.setN2NConfig( config );
                environment.addEnvironmentPeer( p );
                counter++;
            }
        }
    }
}
