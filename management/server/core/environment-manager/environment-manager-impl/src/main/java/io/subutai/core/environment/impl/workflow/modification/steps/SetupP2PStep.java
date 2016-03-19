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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.net.util.SubnetUtils;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.settings.Common;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;
import io.subutai.core.peer.api.PeerManager;


public class SetupP2PStep
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SetupP2PStep.class );
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final PeerManager peerManager;


    public SetupP2PStep( final Topology topology, final EnvironmentImpl environment, final PeerManager peerManager )
    {
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
    }


    public void execute() throws EnvironmentManagerException, InterruptedException, ExecutionException, PeerException
    {
        Set<Peer> peers = peerManager.resolve( topology.getAllPeers() );
        SubnetUtils.SubnetInfo info =
                new SubnetUtils( environment.getTunnelNetwork(), P2PUtil.P2P_SUBNET_MASK ).getInfo();

        //todo use proper p2p secret key here
        String sharedKey = DigestUtils.md5Hex( "secret" );
        final String[] addresses = info.getAllAddresses();
        int counter = environment.getPeerConfs().size();

        for ( Peer peer : peers )
        {
            //todo remove this check since if there are new RHs in participating peers (env grow),
            // P2P needs to be started on them too
            if ( !environment.isMember( peer ) )
            {
                P2PConfig config =
                        new P2PConfig( peer.getId(), environment.getId(), environment.getTunnelInterfaceName(),
                                environment.getTunnelCommunityName(), addresses[counter], sharedKey,
                                Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );
                try
                {
                    //todo run this command in a thread
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