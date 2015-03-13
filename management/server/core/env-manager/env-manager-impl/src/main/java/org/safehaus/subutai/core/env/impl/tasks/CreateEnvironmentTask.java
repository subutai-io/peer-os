package org.safehaus.subutai.core.env.impl.tasks;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.common.network.Gateway;
import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.core.env.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.ResultHolder;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;


public class CreateEnvironmentTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( CreateEnvironmentTask.class.getName() );

    private final LocalPeer localPeer;
    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentImpl environment;
    private final Topology topology;
    private final ResultHolder<EnvironmentCreationException> resultHolder;
    private final Semaphore semaphore;


    public CreateEnvironmentTask( final LocalPeer localPeer, final EnvironmentManagerImpl environmentManager,
                                  final EnvironmentImpl environment, final Topology topology,
                                  final ResultHolder<EnvironmentCreationException> resultHolder )
    {
        this.localPeer = localPeer;
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.topology = topology;
        this.resultHolder = resultHolder;
        this.semaphore = new Semaphore( 0 );
    }


    @Override
    public void run()
    {
        try
        {
            Set<Peer> allPeers = Sets.newHashSet( topology.getNodeGroupPlacement().keySet() );

            //exchange environment certificates
            environmentManager.setupEnvironmentTunnel( environment.getId(), allPeers );

            //check availability of subnet
            Map<Peer, Set<Gateway>> usedGateways = environmentManager.getUsedGateways( allPeers );

            SubnetUtils subnetUtils = new SubnetUtils( environment.getSubnetCidr() );
            String environmentGatewayIp = subnetUtils.getInfo().getLowAddress();

            for ( Map.Entry<Peer, Set<Gateway>> peerGateways : usedGateways.entrySet() )
            {
                Peer peer = peerGateways.getKey();
                Set<Gateway> gateways = peerGateways.getValue();
                for ( Gateway gateway : gateways )
                {
                    if ( gateway.getIp().equals( environmentGatewayIp ) )
                    {
                        LOG.error( String.format( "Subnet %s is already used on peer %s. Peer excluded",
                                environment.getSubnetCidr(), peer.getName() ) );
                        //exclude peer from environment in case subnet is not available
                        topology.excludePeer( peer );
                        break;
                    }
                }
            }

            if ( topology.getNodeGroupPlacement().isEmpty() )
            {
                throw new EnvironmentCreationException( "Subnet is already used on all peers" );
            }

            allPeers = Sets.newHashSet( topology.getNodeGroupPlacement().keySet() );

            //figure out free VNI
            long vni = environmentManager.findFreeVni( allPeers );


            //reserve VNI on local peer
            Vni newVni = new Vni( vni, environment.getId() );

            localPeer.reserveVni( newVni );

            //reserve VNI on remote peers
            allPeers.remove( localPeer );

            for ( Peer peer : allPeers )
            {
                try
                {
                    peer.reserveVni( newVni );
                }
                catch ( PeerException e )
                {
                    LOG.error( String.format( "Failed to reserve VNI %d on peer %s. Peer excluded", vni,
                            peer.getName() ) );
                    //exclude peer from environment in case VNI reservation failed
                    topology.excludePeer( peer );
                }
            }


            if ( topology.getNodeGroupPlacement().isEmpty() )
            {
                throw new EnvironmentCreationException( "Failed to reserve VNI on all remote peers" );
            }

            //save environment VNI
            environment.setVni( vni );

            environmentManager.growEnvironment( environment.getId(), topology, false );
        }
        catch ( Exception e )
        {
            LOG.error( String.format( "Error creating environment %s, topology %s", environment.getId(), topology ),
                    e );

            resultHolder.setResult(
                    e instanceof EnvironmentCreationException ? ( EnvironmentCreationException ) e.getCause() :
                    new EnvironmentCreationException( ExceptionUtils.getRootCause( e ) ) );
        }
        finally
        {
            semaphore.release();
        }
    }


    public void waitCompletion() throws InterruptedException
    {
        semaphore.acquire();
    }
}
