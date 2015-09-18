package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


public class N2NSetupStep
{
    private static final Logger LOGGER = LoggerFactory.getLogger( N2NSetupStep.class );
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


    public void execute() throws EnvironmentManagerException, InterruptedException, ExecutionException
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


        // tunnel setup

        Map<String, String> tunnels = new HashMap();
        for ( PeerConf peerConf : environment.getPeerConfs() )
        {
            tunnels.put( peerConf.getN2NConfig().getPeerId(), peerConf.getN2NConfig().getAddress() );
        }

        int peersCount = environment.getPeerConfs().size();
        ExecutorService tunnelExecutor = Executors.newFixedThreadPool( peersCount );

        ExecutorCompletionService<Integer> tunnelCompletionService =
                new ExecutorCompletionService<Integer>( tunnelExecutor );


        for ( Peer peer : peers )
        {
            tunnelCompletionService.submit( new SetupTunnelTask( peer, environment.getId(), tunnels ) );
        }

        for ( Peer peer : peers )
        {
            final Future<Integer> f = tunnelCompletionService.take();
            Integer vlanId = f.get();
            LOGGER.debug( String.format( "VLAN_ID: %d", vlanId ) );
        }

        tunnelExecutor.shutdown();
    }


    private class SetupTunnelTask implements Callable<Integer>
    {
        private final Peer peer;
        private final String environmentId;
        private final Map<String, String> tunnels;


        public SetupTunnelTask( final Peer peer, final String environmentId, final Map<String, String> tunnels )
        {
            this.peer = peer;
            this.environmentId = environmentId;
            this.tunnels = tunnels;
        }


        @Override
        public Integer call() throws Exception
        {
            return peer.setupTunnels( tunnels, environmentId );
        }
    }
}