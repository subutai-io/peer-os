package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;

import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class ChangeQuotaStep
{

    private final EnvironmentImpl environment;
    private final Map<String, ContainerSize> changedContainers;
    private final TrackerOperation trackerOperation;
    protected TaskUtil<Object> quotaUtil = new TaskUtil<>();


    public ChangeQuotaStep( final EnvironmentImpl environment, final Map<String, ContainerSize> changedContainers,
                            final TrackerOperation trackerOperation )
    {
        this.environment = environment;
        this.changedContainers = changedContainers;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws PeerException
    {
        if ( !CollectionUtil.isMapEmpty( changedContainers ) )
        {

            for ( final Map.Entry<String, ContainerSize> entry : changedContainers.entrySet() )
            {
                final EnvironmentContainerHost containerHost = environment.getContainerHostById( entry.getKey() );
                final ContainerSize containerSize = entry.getValue();

                quotaUtil.addTask( new TaskUtil.Task<Object>()
                {
                    @Override
                    public Object call() throws Exception
                    {
                        containerHost.setContainerSize( containerSize );

                        return null;
                    }
                } );


                TaskUtil.TaskResults<Object> quotaResults = quotaUtil.executeParallel();

                for ( TaskUtil.TaskResult quotaResult : quotaResults.getTaskResults() )
                {
                    if ( quotaResult.hasSucceeded() )
                    {
                        trackerOperation.addLog(
                                String.format( "Container %s's size has been modified", containerHost.getHostname() ) );
                    }
                    else
                    {
                        trackerOperation.addLog( String.format( "Failed to modify size of container %s. Reason: %s",
                                containerHost.getHostname(), quotaResult.getFailureReason() ) );
                    }
                }

                if ( quotaResults.hasFailures() )
                {
                    throw new PeerException( "Failed to change size of each container" );
                }
            }
        }
    }
}
