package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.ArrayList;
import java.util.Collection;
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

import io.subutai.common.environment.Topology;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.util.N2NUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;
import io.subutai.common.peer.LocalPeer;


/**
 * N2N setup step
 */
public class SetupN2NStep
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SetupN2NStep.class );

    private final Topology topology;
    private final EnvironmentImpl env;
    private final LocalPeer localPeer;


    public SetupN2NStep( final Topology topology, final EnvironmentImpl environment, final LocalPeer localPeer )
    {
        this.topology = topology;
        this.env = environment;
        this.localPeer = localPeer;
    }


    public void execute() throws EnvironmentManagerException
    {

        //obtain already participating peers
        Set<Peer> peers = Sets.newHashSet( topology.getAllPeers() );

        peers.add( localPeer );
        // creating new n2n tunnels
        Set<String> existingNetworks = getTunnelNetworks( peers );

        String freeTunnelNetwork = N2NUtil.findFreeTunnelNetwork( existingNetworks );

        LOGGER.debug( String.format( "Free tunnel network: %s", freeTunnelNetwork ) );
        try
        {
            if ( freeTunnelNetwork == null )
            {
                throw new IllegalStateException( "Could not calculate tunnel network." );
            }
            env.setTunnelNetwork( freeTunnelNetwork );
            String sharedKey = "secret";
            SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils( freeTunnelNetwork, N2NUtil.N2N_SUBNET_MASK ).getInfo();
            final String[] addresses = subnetInfo.getAllAddresses();
            int counter = 0;

            ExecutorService n2nExecutor = Executors.newFixedThreadPool( peers.size() );

            ExecutorCompletionService<N2NConfig> n2nCompletionService = new ExecutorCompletionService<>( n2nExecutor );

            // n2n2 setup
            List<N2NConfig> result = new ArrayList<>( peers.size() );
            for ( Peer peer : peers )
            {
                N2NConfig config = new N2NConfig( peer.getId(), env.getId(), env.getSuperNode(), env.getSuperNodePort(),
                        env.getTunnelInterfaceName(), env.getTunnelCommunityName(), addresses[counter], sharedKey );
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
                env.addEnvironmentPeer( new PeerConfImpl( config ) );
            }

            // tunnel setup
            Map<String, String> tunnels = env.getTunnels();

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
                f.get();
            }

            tunnelExecutor.shutdown();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new EnvironmentManagerException( "Could not create n2n tunnel.", e );
        }
    }


    private Set<String> getTunnelNetworks( final Set<Peer> peers )
    {
        Set<String> result = new HashSet<>();

        for ( Peer peer : peers )
        {
            Set<HostInterfaceModel> r = peer.getInterfaces().filterByIp( N2NUtil.N2N_INTERFACE_IP_PATTERN );

            Collection tunnels = CollectionUtils.collect( r, new Transformer()
            {
                @Override
                public Object transform( final Object o )
                {
                    HostInterfaceModel i = ( HostInterfaceModel ) o;
                    SubnetUtils u = new SubnetUtils( i.getIp(), N2NUtil.N2N_SUBNET_MASK );
                    return u.getInfo().getNetworkAddress();
                }
            } );

            result.addAll( tunnels );
        }

        return result;
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
