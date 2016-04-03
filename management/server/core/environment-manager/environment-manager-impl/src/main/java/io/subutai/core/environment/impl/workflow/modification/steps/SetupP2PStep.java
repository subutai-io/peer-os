package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.RhP2PIpEntity;


public class SetupP2PStep
{
    private static final Logger LOG = LoggerFactory.getLogger( SetupP2PStep.class );
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final TrackerOperation trackerOperation;


    public SetupP2PStep( final Topology topology, final EnvironmentImpl environment,
                         final TrackerOperation trackerOperation )
    {
        this.topology = topology;
        this.environment = environment;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentModificationException, PeerException
    {

        //create p2p subnet util
        SubnetUtils.SubnetInfo p2pSubnetInfo =
                new SubnetUtils( environment.getP2pSubnet(), P2PUtil.P2P_SUBNET_MASK ).getInfo();

        //get all subnet ips
        final Set<String> p2pAddresses = Sets.newHashSet( p2pSubnetInfo.getAllAddresses() );

        //subtract already used ips
        for ( RhP2pIp rhP2pIp : environment.getP2pIps().getP2pIps() )
        {
            p2pAddresses.remove( rhP2pIp.getP2pIp() );
        }

        //obtain target RHs
        Map<String, Set<String>> peerRhIds = topology.getPeerRhIds();

        //count total requested
        int totalIps = 0;

        for ( Set<String> peerRhs : peerRhIds.values() )
        {
            totalIps += peerRhs.size();
        }

        if ( totalIps > p2pAddresses.size() )
        {
            throw new EnvironmentModificationException(
                    String.format( "Requested IP count %d is more than available %d", totalIps, p2pAddresses.size() ) );
        }

        //obtain participating peers
        Set<Peer> peers = environment.getPeers();

        //generate p2p secret key
        String sharedKey = DigestUtils.md5Hex( UUID.randomUUID().toString() );

        ExecutorService p2pExecutor = Executors.newFixedThreadPool( peers.size() );
        ExecutorCompletionService<P2PConfig> p2pCompletionService = new ExecutorCompletionService<>( p2pExecutor );

        Iterator<String> p2pAddressIterator = p2pAddresses.iterator();

        //p2p setup
        for ( Peer peer : peers )
        {
            P2PConfig config = new P2PConfig( peer.getId(), environment.getId(), environment.getP2PHash(), sharedKey,
                    Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );

            //obtain target RHs
            Set<String> rhIds = peerRhIds.get( peer.getId() );

            if ( !CollectionUtil.isCollectionEmpty( rhIds ) )
            {
                for ( String rhId : rhIds )
                {
                    config.addRhP2pIp( new RhP2PIpEntity( rhId, p2pAddressIterator.next() ) );
                }
            }

            p2pCompletionService.submit( new SetupP2PConnectionTask( peer, config ) );
        }

        Set<Peer> succeededPeers = Sets.newHashSet();
        List<P2PConfig> result = new ArrayList<>( peers.size() );
        for ( Peer peer : peers )
        {
            try
            {
                final Future<P2PConfig> f = p2pCompletionService.take();
                P2PConfig config = f.get();
                result.add( config );
                succeededPeers.add( peer );
            }
            catch ( Exception e )
            {
                LOG.error( "Problems setting up p2p connection", e );
            }
        }

        p2pExecutor.shutdown();

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation.addLog( String.format( "P2P setup succeeded on peer %s", succeededPeer.getName() ) );
        }

        peers.removeAll( succeededPeers );

        for ( Peer failedPeer : peers )
        {
            trackerOperation.addLog( String.format( "P2P setup failed on peer %s", failedPeer.getName() ) );
        }

        for ( P2PConfig config : result )
        {
            environment.getPeerConf( config.getPeerId() ).addRhP2pIps( config.getRhP2pIps() );
        }

        if ( !peers.isEmpty() )
        {
            throw new EnvironmentModificationException( "Failed to setup P2P connection across all peers" );
        }

        // tunnel setup
        P2pIps p2pIps = environment.getP2pIps();
        int peersCount = environment.getPeerConfs().size();
        ExecutorService tunnelExecutor = Executors.newFixedThreadPool( peersCount );

        ExecutorCompletionService<Boolean> tunnelCompletionService = new ExecutorCompletionService<>( tunnelExecutor );

        for ( Peer peer : environment.getPeers() )
        {
            tunnelCompletionService.submit( new SetupTunnelTask( peer, environment.getId(), p2pIps ) );
        }

        succeededPeers = Sets.newHashSet();
        peers = environment.getPeers();

        for ( Peer peer : peers )
        {
            final Future<Boolean> f;
            try
            {
                f = tunnelCompletionService.take();
                f.get();
                succeededPeers.add( peer );
            }
            catch ( Exception e )
            {
                LOG.error( "Problems setting up tunnels", e );
            }
        }

        tunnelExecutor.shutdown();

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation.addLog( String.format( "Tunnel setup succeeded on peer %s", succeededPeer.getName() ) );
        }

        peers.removeAll( succeededPeers );

        for ( Peer failedPeer : peers )
        {
            trackerOperation.addLog( String.format( "Tunnel setup failed on peer %s", failedPeer.getName() ) );
        }

        if ( !peers.isEmpty() )
        {
            throw new EnvironmentModificationException( "Failed to setup tunnel across all peers" );
        }
    }


    private class SetupP2PConnectionTask implements Callable<P2PConfig>
    {
        private Peer peer;
        private P2PConfig p2PConfig;


        public SetupP2PConnectionTask( final Peer peer, final P2PConfig config )
        {
            this.peer = peer;
            this.p2PConfig = config;
        }


        @Override
        public P2PConfig call() throws Exception
        {
            peer.joinOrUpdateP2PSwarm( p2PConfig );

            return p2PConfig;
        }
    }


    private class SetupTunnelTask implements Callable<Boolean>
    {
        private final Peer peer;
        private final String environmentId;
        private final P2pIps p2pIps;


        public SetupTunnelTask( final Peer peer, final String environmentId, final P2pIps p2pIps )
        {
            this.peer = peer;
            this.environmentId = environmentId;
            this.p2pIps = p2pIps;
        }


        @Override
        public Boolean call() throws Exception
        {
            peer.setupTunnels( p2pIps, environmentId );

            return true;
        }
    }
}