package io.subutai.core.environment.impl.workflow.modification.steps;


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

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;


public class SetupP2PStep
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SetupP2PStep.class );
    private final Topology topology;
    private final EnvironmentImpl environment;


    public SetupP2PStep( final Topology topology, final EnvironmentImpl environment )
    {
        this.topology = topology;
        this.environment = environment;
    }


    public void execute() throws EnvironmentManagerException, InterruptedException, ExecutionException
    {
        Set<Peer> peers = Sets.newHashSet( topology.getAllPeers() );

        SubnetUtils.SubnetInfo info =
                new SubnetUtils( environment.getTunnelNetwork(), P2PUtil.P2P_SUBNET_MASK ).getInfo();

        String sharedKey = "secret";
        final String[] addresses = info.getAllAddresses();
        int counter = environment.getPeerConfs().size();
        for ( Peer peer : peers )
        {
            if ( !environment.isMember( peer ) )
            {
                P2PConfig config = new P2PConfig( peer.getId(), environment.getId(), environment.getSuperNode(),
                        environment.getSuperNodePort(), environment.getTunnelInterfaceName(),
                        environment.getTunnelCommunityName(), addresses[counter], sharedKey );
                try
                {
                    peer.setupP2PConnection( config );
                    environment.addEnvironmentPeer( new PeerConfImpl( config ) );
                }
                catch ( PeerException e )
                {
                    throw new EnvironmentManagerException( "Could not create P2P connection on peer: " + peer.getId(),
                            e );
                }
                counter++;
            }
        }


        // tunnel setup

        Map<String, String> tunnels = environment.getTunnels();

        ExecutorService tunnelExecutor = Executors.newFixedThreadPool( peers.size() );

        ExecutorCompletionService<Integer> tunnelCompletionService =
                new ExecutorCompletionService<Integer>( tunnelExecutor );

        for ( Peer peer : environment.getPeers() )
        {
            tunnelCompletionService.submit( new SetupTunnelTask( peer, environment.getId(), tunnels ) );
        }

        for ( Peer peer : environment.getPeers() )
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