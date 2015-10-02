package io.subutai.core.environment.impl.workflow.destruction.steps;


import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainersDestructionResult;
import io.subutai.common.peer.Peer;
import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.destruction.steps.helpers.EnvironmentContainerGroupDestructionTask;


public class DestroyContainersStep
{
    private final Environment environment;
    private final EnvironmentManagerImpl environmentManager;
    private final boolean forceMetadataRemoval;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();


    public DestroyContainersStep( final Environment environment, final EnvironmentManagerImpl environmentManager,
                                  final boolean forceMetadataRemoval )
    {
        Preconditions.checkNotNull( environment );

        this.environmentManager = environmentManager;
        this.environment = environment;
        this.forceMetadataRemoval = forceMetadataRemoval;
    }


    protected ExecutorService getExecutor( Environment environment )
    {
        return Executors.newFixedThreadPool( environment.getPeers().size() );
    }


    public void execute() throws EnvironmentDestructionException, EnvironmentNotFoundException
    {

        if ( environment.getStatus() == EnvironmentStatus.EMPTY || environment.getContainerHosts().isEmpty() )
        {
            environmentManager.removeEnvironment( environment.getId() );
            return;
        }


        ExecutorService executorService = getExecutor( environment );

        try
        {
            Set<Throwable> exceptions = Sets.newHashSet();

            Set<Future<ContainersDestructionResult>> futures = Sets.newHashSet();

            for ( Peer peer : environment.getPeers() )
            {
                futures.add( executorService
                        .submit( new EnvironmentContainerGroupDestructionTask( peer, environment.getId() ) ) );
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
                    Throwable cause = exceptionUtil.getRootCause( e );

                    exceptions.add( cause );
                }
            }

            for ( ContainersDestructionResult result : results )
            {
                boolean deleteAllPeerContainers = false;
                if ( !Strings.isNullOrEmpty( result.getException() ) )
                {

                    if ( result.getException().equals( Common.CONTAINER_GROUP_NOT_FOUND ) )
                    {
                        deleteAllPeerContainers = true;
                    }
                    else
                    {
                        exceptions.add( new EnvironmentDestructionException( result.getException() ) );
                    }
                }
                if ( deleteAllPeerContainers )
                {
                    for ( ContainerHost containerHost : environment.getContainerHosts() )
                    {
                        if ( containerHost.getPeerId().equals( result.peerId() ) )
                        {
                            environment.removeContainer( containerHost );

                            environmentManager.notifyOnContainerDestroyed( environment, containerHost.getId() );
                        }
                    }
                }
                else if ( !CollectionUtil.isCollectionEmpty( result.getDestroyedContainersIds() ) )
                {
                    for ( ContainerHost container : result.getDestroyedContainersIds() )
                    {
                        environment.removeContainer( container );

                        environmentManager.notifyOnContainerDestroyed( environment, container.getId() );
                    }
                }
            }


            //            if ( forceMetadataRemoval || environment.getContainerHosts().isEmpty() )
            //            {
            //                environmentManager.removeEnvironment( environment.getId()/*, false*/ );
            //            }
            //            else
            //            {
            //                throw new EnvironmentDestructionException(
            //                        String.format( "There were errors while destroying environment: %s", exceptions
            // ) );
            //            }
        }
        finally
        {
            executorService.shutdown();
        }
    }
}
