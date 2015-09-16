package io.subutai.core.env.impl.tasks;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.env.impl.EnvironmentManagerImpl;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.ResultHolder;


/**
 * Add specified number of environment hosts to existing environment.
 *
 * @see io.subutai.core.env.impl.entity.EnvironmentImpl
 * @see io.subutai.core.env.impl.exception.ResultHolder
 * @see io.subutai.common.environment.Topology
 * @see io.subutai.common.peer.ContainerHost
 */
public class GrowEnvironmentTask implements Awaitable
{
    private static final Logger LOG = LoggerFactory.getLogger( GrowEnvironmentTask.class.getName() );

    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentImpl environment;
    private final Topology topology;
    private final ResultHolder<EnvironmentModificationException> resultHolder;
    private final Set<ContainerHost> newContainers;
    private final TrackerOperation op;
    protected Semaphore semaphore;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();


    public GrowEnvironmentTask( final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
                                final Topology topology,
                                final ResultHolder<EnvironmentModificationException> resultHolder,
                                final Set<ContainerHost> newContainers, final TrackerOperation op )
    {
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.topology = topology;
        this.resultHolder = resultHolder;
        this.newContainers = newContainers;
        this.semaphore = new Semaphore( 0 );
        this.op = op;
    }


    @Override
    public void run()
    {
        try
        {
            environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );


            final Set<ContainerHost> oldContainers = Sets.newHashSet( environment.getContainerHosts() );

            try
            {
                Set<Peer> newRemotePeers = Sets.newHashSet( topology.getNodeGroupPlacement().keySet() );
                newRemotePeers.removeAll( environment.getPeers() );

                if ( !newRemotePeers.isEmpty() )
                {
                    op.addLog( "Setting up n2n tunnel..." );
                    Set<Peer> allPeers = new HashSet<>( environment.getPeers() );
                    allPeers.addAll( topology.getAllPeers() );
                    environmentManager.setupN2NConnection( environment, allPeers );
                    for ( Peer p : newRemotePeers )
                    {
                        String pekId = String.format( "%s-%s", p.getId(), environment.getId() );
                        p.createEnvironmentKeyPair( pekId );
                    }
                }

                op.addLog( "Ensuring secure channel..." );

                op.addLog( "Cloning containers..." );

                environmentManager.build( environment, topology );

                newContainers.addAll( environment.getContainerHosts() );

                newContainers.removeAll( oldContainers );

                environmentManager.setContainersTransientFields( environment );

                op.addLog( "Configuring /etc/hosts..." );

                environmentManager.configureHosts( environment.getContainerHosts() );

                op.addLog( "Configuring ssh..." );

                environmentManager.configureSsh( environment.getContainerHosts() );

                if ( !Strings.isNullOrEmpty( environment.getSshKey() ) )
                {
                    op.addLog( "Setting environment ssh key.." );

                    environmentManager.setSshKey( environment.getId(), environment.getSshKey(), false, false, op );
                }

                environment.setStatus( EnvironmentStatus.HEALTHY );

                op.addLogDone( "Environment grown successfully" );
            }
            catch ( Exception e )
            {
                environment.setStatus( EnvironmentStatus.UNHEALTHY );

                throw new EnvironmentModificationException( exceptionUtil.getRootCause( e ) );
            }
            finally
            {
                if ( !newContainers.isEmpty() )
                {
                    environmentManager.notifyOnEnvironmentGrown( environment, newContainers );
                }
            }
        }
        catch ( EnvironmentModificationException e )
        {
            LOG.error( String.format( "Error growing environment %s, topology %s", environment.getId(), topology ), e );

            resultHolder.setResult( e );

            op.addLogFailed( String.format( "Error growing environment: %s", resultHolder.getResult().getMessage() ) );
        }
        finally
        {
            semaphore.release();
        }
    }


    @Override
    public void waitCompletion() throws InterruptedException
    {
        semaphore.acquire();
    }
}
