package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.List;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.modification.steps.helpers.ContainerDestroyTask;
import io.subutai.common.util.TaskUtil;


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


    public void execute() throws Exception
    {
        if ( !CollectionUtil.isCollectionEmpty( removedContainers ) )
        {
            TaskUtil<Object> destroyUtil = new TaskUtil<>();

            for ( String containerId : removedContainers )
            {
                final ContainerHost containerHost = environment.getContainerHostById( containerId );

                destroyUtil.addTask( new ContainerDestroyTask( containerHost ) );
            }

            TaskUtil.TaskResults<Object> destroyResults = destroyUtil.executeParallel();

            for ( TaskUtil.TaskResult<Object> destroyResult : destroyResults.getTaskResults() )
            {
                ContainerHost containerHost = ( ( ContainerDestroyTask ) destroyResult.getTask() ).getContainerHost();

                if ( destroyResult.hasSucceeded() )
                {
                    environment.removeContainer( containerHost );

                    environmentManager.notifyOnContainerDestroyed( environment, containerHost.getId() );

                    trackerOperation.addLog( String.format( "Container %s destroyed", containerHost.getHostname() ) );
                }
                else
                {
                    trackerOperation.addLog(
                            String.format( "Failed to destroy container %s. Reason: %s", containerHost.getHostname(),
                                    destroyResult.getFailureReason() ) );
                }
            }
        }
    }
}
