package org.safehaus.subutai.core.env.impl.tasks;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.environment.EnvironmentNotFoundException;
import org.safehaus.subutai.common.environment.EnvironmentStatus;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.ContainersDestructionResult;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.env.api.exception.EnvironmentDestructionException;
import org.safehaus.subutai.core.env.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.env.impl.PeerEnvironmentDestructionTask;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.ResultHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Destroys Environment with all values carried with it. Destroys all container hosts across peers if any.
 *
 * @see org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl
 * @see org.safehaus.subutai.core.env.impl.EnvironmentManagerImpl
 * @see org.safehaus.subutai.core.env.impl.exception.ResultHolder
 * @see java.lang.Runnable
 */
public class DestroyEnvironmentTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( DestroyEnvironmentTask.class.getName() );

    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentImpl environment;
    private final Set<EnvironmentDestructionException> exceptions;
    private final ResultHolder<EnvironmentDestructionException> resultHolder;
    private final boolean forceMetadataRemoval;
    private final Semaphore semaphore;
    private final Set<Peer> peerStoresToUpdate;

    public DestroyEnvironmentTask( final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
                                   final Set<EnvironmentDestructionException> exceptions,
                                   final ResultHolder<EnvironmentDestructionException> resultHolder,
                                   final boolean forceMetadataRemoval, final Set<Peer> peersToRemoveCertFrom )
    {
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.exceptions = exceptions;
        this.resultHolder = resultHolder;
        this.forceMetadataRemoval = forceMetadataRemoval;
        this.semaphore = new Semaphore( 0 );
        this.peerStoresToUpdate = peersToRemoveCertFrom;
    }


    @Override
    public void run()
    {
        try
        {

            if ( environment.getStatus() == EnvironmentStatus.EMPTY || environment.getContainerHosts().isEmpty() )
            {
                environmentManager.removeEnvironment( environment.getId() );
                return;
            }

            environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

            Set<Peer> environmentPeers = Sets.newHashSet();

            for ( ContainerHost container : environment.getContainerHosts() )
            {
                environmentPeers.add( container.getPeer() );
            }

            ExecutorService executorService = Executors.newFixedThreadPool( environmentPeers.size() );

            Set<Future<ContainersDestructionResult>> futures = Sets.newHashSet();

            for ( Peer peer : environmentPeers )
            {
                futures.add(
                        executorService.submit( new PeerEnvironmentDestructionTask( peer, environment.getId() ) ) );
            }

            Set<ContainersDestructionResult> results = Sets.newHashSet();

            for ( Future<ContainersDestructionResult> future : futures )
            {
                try
                {
                    results.add( future.get() );
                }
                catch ( ExecutionException | InterruptedException e )
                {
                    exceptions.add( new EnvironmentDestructionException( e ) );
                }
            }

            executorService.shutdown();

            for ( ContainersDestructionResult result : results )
            {
                boolean deleteAllPeerContainers = false;
                if ( !Strings.isNullOrEmpty( result.getException() ) )
                {
                    exceptions.add( new EnvironmentDestructionException( result.getException() ) );

                    if ( result.getException().equals( "Container group not found" ) )
                    {
                        deleteAllPeerContainers = true;
                    }
                }
                if ( deleteAllPeerContainers )
                {
                    for ( ContainerHost containerHost : environment.getContainerHosts() )
                    {
                        if ( containerHost.getPeerId().equals( result.peerId().toString() ) )
                        {
                            environment.removeContainer( containerHost.getId() );

                            environmentManager.notifyOnContainerDestroyed( environment, containerHost.getId() );
                        }
                    }
                }
                else if ( !CollectionUtil.isCollectionEmpty( result.getDestroyedContainersIds() ) )
                {
                    for ( UUID containerId : result.getDestroyedContainersIds() )
                    {
                        environment.removeContainer( containerId );

                        environmentManager.notifyOnContainerDestroyed( environment, containerId );
                    }
                }
            }

            for ( final Peer peer : peerStoresToUpdate )
            {
                peer.removeEnvironmentCertificates( environment.getId() );
            }

            if ( forceMetadataRemoval || environment.getContainerHosts().isEmpty() )
            {
                environmentManager.removeEnvironment( environment.getId() );
            }
            else
            {
                environment.setStatus( EnvironmentStatus.UNHEALTHY );
            }
        }
        catch ( EnvironmentNotFoundException | PeerException e )
        {
            LOG.error( String.format( "Error destroying environment %s", environment.getId() ), e );

            resultHolder.setResult( new EnvironmentDestructionException( e ) );
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
