package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;

import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.bazaar.share.quota.ContainerQuota;


public class ChangeQuotaStep
{

    private final LocalEnvironment environment;
    private final Map<String, ContainerQuota> changedContainers;
    private final TrackerOperation trackerOperation;
    protected TaskUtil<Object> quotaUtil = new TaskUtil<>();


    public ChangeQuotaStep( final LocalEnvironment environment, final Map<String, ContainerQuota> changedContainers,
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
            for ( final Map.Entry<String, ContainerQuota> entry : changedContainers.entrySet() )
            {
                final EnvironmentContainerHost containerHost = environment.getContainerHostById( entry.getKey() );
                final ContainerQuota containerQuota = entry.getValue();

                quotaUtil.addTask( new TaskUtil.Task<Object>()
                {
                    @Override
                    public Object call() throws Exception
                    {
                        containerHost.setQuota( containerQuota );

                        return null;
                    }
                } );


                TaskUtil.TaskResults<Object> quotaResults = quotaUtil.executeParallel();

                for ( TaskUtil.TaskResult quotaResult : quotaResults.getResults() )
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
