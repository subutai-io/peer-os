package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.List;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.modification.steps.helpers.ContainerDestroyTask;


/**
 * Container destroy step
 */
public class DestroyContainersStep
{
    private final EnvironmentImpl environment;
    private final EnvironmentManagerImpl environmentManager;
    private final List<String> removedContainers;
    private final TrackerOperation trackerOperation;


    public DestroyContainersStep( final EnvironmentImpl environment, final EnvironmentManagerImpl environmentManager,
                                  final List<String> removedContainers, TrackerOperation trackerOperation )
    {
        this.environment = environment;
        this.environmentManager = environmentManager;
        this.removedContainers = removedContainers;
        this.trackerOperation = trackerOperation;
    }


    public Environment execute() throws Exception
    {
        if ( !CollectionUtil.isCollectionEmpty( removedContainers ) )
        {
            TaskUtil<Environment> destroyUtil = new TaskUtil<>();

            for ( String containerId : removedContainers )
            {
                final ContainerHost containerHost = environment.getContainerHostById( containerId );

                destroyUtil.addTask( new ContainerDestroyTask( containerHost ) );
            }

            TaskUtil.TaskResults<Environment> destroyResults = destroyUtil.executeParallel();

            for ( TaskUtil.TaskResult<Environment> destroyResult : destroyResults.getTaskResults() )
            {
                ContainerHost containerHost = ( ( ContainerDestroyTask ) destroyResult.getTask() ).getContainerHost();

                if ( destroyResult.hasSucceeded() )
                {
                    trackerOperation.addLog( String.format( "Container %s destroyed", containerHost.getHostname() ) );
                }
                else
                {
                    trackerOperation.addLog(
                            String.format( "Failed to destroy container %s. Reason: %s", containerHost.getHostname(),
                                    destroyResult.getFailureReason() ) );
                }
            }

            return environmentManager.loadEnvironment( environment.getId() );
        }

        return environment;
    }
}
