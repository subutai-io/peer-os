package org.safehaus.subutai.core.env.impl.tasks;


import java.util.Set;
import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.environment.Topology;
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
            //figure out free VNI
            long vni = environmentManager.findFreeVni( topology.getNodeGroupPlacement().keySet() );

            //reserve VNI on all peers
            Set<Peer> allPeers = Sets.newHashSet( topology.getNodeGroupPlacement().keySet() );
            //add local peer mandatorily
            allPeers.add( localPeer );

            for ( Peer peer : allPeers )
            {
                try
                {
                    peer.reserveVni( new Vni( vni, environment.getId() ) );
                }
                catch ( PeerException e )
                {
                    if ( peer.isLocal() )
                    {
                        throw new EnvironmentCreationException( e );
                    }
                    else
                    {
                        LOG.error( String.format( "Failed to reserve VNI %d on peer %s. Peer excluded", vni,
                                peer.getName() ) );
                        topology.excludePeer( peer );
                    }
                }
            }


            if ( topology.getNodeGroupPlacement().keySet().isEmpty() )
            {
                throw new EnvironmentCreationException( "Failed to reserve VNi on all peers" );
            }

            //setup tunnels to all participating peers on local peer in case local peer is not included as provider peer
            Set<String> peerIps = Sets.newHashSet();

            peerIps.add( localPeer.getManagementHost().getIpByInterfaceName( "eth1" ) );

            for ( Peer peer : topology.getNodeGroupPlacement().keySet() )
            {
                if ( !peer.getId().equals( localPeer.getId() ) )
                {
                    peerIps.add( peer.getPeerInfo().getIp() );
                }
            }


            int vlan = localPeer.setupTunnels( peerIps, new Vni( vni, environment.getId() ) );


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
                    new EnvironmentCreationException( e ) );
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
