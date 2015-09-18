package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.PeerConf;
import io.subutai.common.environment.Topology;
import io.subutai.common.host.Interface;
import io.subutai.common.peer.InterfacePattern;
import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.util.N2NUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.PeerConfImpl;
import io.subutai.core.peer.api.LocalPeer;


/**
 * N2N setup step
 */
public class N2NSetupStep
{
    private static final Logger LOGGER = LoggerFactory.getLogger( N2NSetupStep.class );
    private final Topology topology;
    private final Environment env;
    private final String supernode;
    private final int supernodePort;
    private final LocalPeer localPeer;


    public N2NSetupStep( final Topology topology, final Environment environment, final String supernode,
                         final int supernodePort, final LocalPeer localPeer )
    {
        this.topology = topology;
        this.env = environment;
        this.supernode = supernode;
        this.supernodePort = supernodePort;
        this.localPeer = localPeer;
    }


    public void execute() throws EnvironmentManagerException
    {

        //obtain already participating peers
        Set<Peer> peers = Sets.newHashSet( topology.getAllPeers() );

        peers.add( localPeer );
        // creating new n2n tunnels
        Set<String> allSubnets = getSubnets( peers );
        LOGGER.debug( String.format( "Found %d peer subnets:", allSubnets.size() ) );
        for ( String s : allSubnets )
        {
            LOGGER.debug( s );
        }

        String freeSubnet = N2NUtil.findFreeSubnet( allSubnets );

        LOGGER.debug( String.format( "Free subnet for peer: %s", freeSubnet ) );
        try
        {
            if ( freeSubnet == null )
            {
                throw new IllegalStateException( "Could not calculate subnet." );
            }
            String interfaceName = N2NUtil.generateInterfaceName( freeSubnet );
            String communityName = N2NUtil.generateCommunityName( freeSubnet );
            String sharedKey = "secret";
            SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils( freeSubnet, N2NUtil.N2N_SUBNET_MASK ).getInfo();
            final String[] addresses = subnetInfo.getAllAddresses();
            int counter = 0;

            ExecutorService n2nExecutor = Executors.newFixedThreadPool( peers.size() );

            ExecutorCompletionService<N2NConfig> n2nCompletionService = new ExecutorCompletionService<>( n2nExecutor );


            // n2n2 setup
            List<N2NConfig> result = new ArrayList<>( peers.size() );
            for ( Peer peer : peers )
            {
                N2NConfig config = new N2NConfig( peer.getId(), supernode, supernodePort, interfaceName, communityName,
                        addresses[counter], sharedKey );
                n2nCompletionService.submit( new SetupN2NConnectionTask( peer, config ) );
                counter++;
            }

            for ( Peer peer : peers )
            {
                final Future<N2NConfig> f = n2nCompletionService.take();
                N2NConfig config = f.get();
                result.add( config );
            }

            n2nExecutor.shutdown();

            for ( N2NConfig config : result )
            {
                final PeerConf p = new PeerConfImpl();
                p.setN2NConfig( config );
                env.addEnvironmentPeer( p );
            }

            // tunnel setup
            Map<String, String> tunnels = new HashMap();
            for ( PeerConf peerConf : env.getPeerConfs() )
            {
                tunnels.put( peerConf.getN2NConfig().getPeerId(), peerConf.getN2NConfig().getAddress() );
            }


            int peersCount = env.getPeerConfs().size();
            ExecutorService tunnelExecutor = Executors.newFixedThreadPool( peersCount );

            ExecutorCompletionService<Integer> tunnelCompletionService =
                    new ExecutorCompletionService<Integer>( tunnelExecutor );


            for ( Peer peer : peers )
            {
                tunnelCompletionService.submit( new SetupTunnelTask( peer, env.getId(), tunnels ) );
            }

            for ( int i = 0; i < peersCount; i++ )
            {
                final Future<Integer> f = tunnelCompletionService.take();
                Integer vlanId = f.get();
                LOGGER.debug( String.format( "VLAN_ID: %d", vlanId ) );
            }

            tunnelExecutor.shutdown();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new EnvironmentManagerException( "Could not create n2n tunnel.", e );
        }
    }


    private Set<String> getSubnets( final Set<Peer> allPeers )
    {
        Set<String> allSubnets = new HashSet<>();

        InterfacePattern peerSubnetsPattern = new InterfacePattern( "ip", "^10.*" );
        for ( Peer peer : allPeers )
        {
            Set<Interface> r = peer.getNetworkInterfaces( peerSubnetsPattern );

            Collection peerSubnets = CollectionUtils.collect( r, new Transformer()
            {
                @Override
                public Object transform( final Object o )
                {
                    Interface i = ( Interface ) o;
                    SubnetUtils u = new SubnetUtils( i.getIp(), N2NUtil.N2N_SUBNET_MASK );
                    return u.getInfo().getNetworkAddress();
                }
            } );

            allSubnets.addAll( peerSubnets );
        }

        return allSubnets;
    }


    private class SetupN2NConnectionTask implements Callable<N2NConfig>
    {
        private Peer peer;
        private N2NConfig n2NConfig;


        public SetupN2NConnectionTask( final Peer peer, final N2NConfig config )
        {
            this.peer = peer;
            this.n2NConfig = config;
        }


        @Override
        public N2NConfig call() throws Exception
        {
            peer.setupN2NConnection( n2NConfig );
            return n2NConfig;
        }
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
