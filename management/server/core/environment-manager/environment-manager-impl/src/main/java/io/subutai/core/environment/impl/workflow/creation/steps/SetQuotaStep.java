package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;

import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.hub.share.quota.ContainerQuota;


public class SetQuotaStep
{
    private final TrackerOperation trackerOperation;
    private final Map<EnvironmentContainerImpl, ContainerQuota> containerQuotas;
    protected PeerUtil<Object> peerUtil = new PeerUtil<>();
    protected TaskUtil<Object> quotaUtil = new TaskUtil<>();


    public SetQuotaStep( final Map<EnvironmentContainerImpl, ContainerQuota> containerQuotas,
                         final TrackerOperation trackerOperation )
    {
        this.containerQuotas = containerQuotas;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentManagerException, PeerException
    {
        for ( final Map.Entry<EnvironmentContainerImpl, ContainerQuota> entry : containerQuotas.entrySet() )
        {
            final EnvironmentContainerHost containerHost = entry.getKey();
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
                            String.format( "Container %s's quota has been set", containerHost.getHostname() ) );
                }
                else
                {
                    trackerOperation.addLog( String.format( "Failed to set quota of container %s. Reason: %s",
                            containerHost.getHostname(), quotaResult.getFailureReason() ) );
                }
            }

            if ( quotaResults.hasFailures() )
            {
                throw new PeerException( "Failed to change quota of each container" );
            }
        }
    }
}
