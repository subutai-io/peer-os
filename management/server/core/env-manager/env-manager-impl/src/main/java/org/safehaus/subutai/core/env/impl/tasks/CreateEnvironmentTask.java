package org.safehaus.subutai.core.env.impl.tasks;


import java.util.Set;
import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.common.peer.Peer;
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

            //setup tunnels to all remote peers on local peer
            Set<String> remotePeerIps = Sets.newHashSet();
            for ( Peer peer : topology.getNodeGroupPlacement().keySet() )
            {
                if ( !peer.getId().equals( localPeer.getId() ) )
                {
                    remotePeerIps.add( peer.getPeerInfo().getIp() );
                }
            }

            //TODO fix when environment is created on local peer only
            //            if ( !remotePeerIps.isEmpty() )
            //            {
            int vlan = localPeer.setupTunnels( remotePeerIps, vni, true );

            //save container group
            localPeer.createEmptyContainerGroup( environment.getId(), localPeer.getId(), localPeer.getOwnerId(), vni,
                    vlan );
            //            }

            environment.setVni( vni );

            environmentManager.growEnvironment( environment.getId(), topology, false );
        }
        catch ( Exception e )
        {
            LOG.error( String.format( "Error creating environment %s, topology %s", environment.getId(), topology ),
                    e );

            resultHolder.setResult( new EnvironmentCreationException( e ) );
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
