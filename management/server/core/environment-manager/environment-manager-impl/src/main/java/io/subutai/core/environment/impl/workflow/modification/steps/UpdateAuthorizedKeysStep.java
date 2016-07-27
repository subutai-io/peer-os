package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.Environment;
import io.subutai.common.host.HostId;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class UpdateAuthorizedKeysStep
{
    private final EnvironmentImpl environment;
    private final Map<HostId, String> oldContainerHostNames;
    private final Map<HostId, String> newContainerHostNames;
    private final TrackerOperation trackerOperation;


    public UpdateAuthorizedKeysStep( final EnvironmentImpl environment, final Map<HostId, String> oldContainerHostNames,
                                     final Map<HostId, String> newContainerHostNames,
                                     TrackerOperation trackerOperation )
    {
        this.environment = environment;
        this.oldContainerHostNames = oldContainerHostNames;
        this.newContainerHostNames = newContainerHostNames;
        this.trackerOperation = trackerOperation;
    }


    public Environment execute() throws Exception
    {
        //todo parallelize across peer (use Peer interface)
        boolean ok = true;

        for ( EnvironmentContainerHost environmentContainer : environment.getContainerHosts() )
        {
            for ( Map.Entry<HostId, String> oldHostname : oldContainerHostNames.entrySet() )
            {
                try
                {
                    environmentContainer.execute( new RequestBuilder(
                            String.format( "chmod 700 %3$s && sed -i 's/%1$s/%2$s/g' %3$s && chmod 644 %3$s",
                                    oldHostname.getValue(), newContainerHostNames.get( oldHostname.getKey() ),
                                    Common.CONTAINER_SSH_FILE ) ) );
                }
                catch ( Exception e )
                {
                    ok = false;

                    trackerOperation.addLog( String.format( "Failed to update authorized_keys file of container %s: %s",
                            oldHostname.getKey().getId(), e.getMessage() ) );
                }
            }
        }

        //todo review may be we should complete the whole chain without throwing exception
        if ( !ok )
        {
            throw new PeerException( "Failed to update all containers' authorized_keys files" );
        }

        return environment;
    }
}
