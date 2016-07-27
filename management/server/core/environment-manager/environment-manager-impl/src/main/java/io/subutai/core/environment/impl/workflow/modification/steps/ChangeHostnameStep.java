package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import io.subutai.common.environment.Environment;
import io.subutai.common.host.HostId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class ChangeHostnameStep
{
    private final EnvironmentManagerImpl environmentManager;

    private final EnvironmentImpl environment;
    private final Map<HostId, String> newContainerHostNames;
    private final TrackerOperation trackerOperation;
    private final Map<HostId, String> oldHostNames = Maps.newConcurrentMap();


    public ChangeHostnameStep( final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
                               final Map<HostId, String> newContainerHostNames, TrackerOperation trackerOperation )
    {
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.newContainerHostNames = newContainerHostNames;
        this.trackerOperation = trackerOperation;
    }


    public Environment execute() throws Exception
    {

        //todo parallelize
        boolean ok = true;

        for ( Map.Entry<HostId, String> newHostnameEntry : newContainerHostNames.entrySet() )
        {
            try
            {
                EnvironmentContainerImpl environmentContainer = ( EnvironmentContainerImpl ) environment
                        .getContainerHostById( newHostnameEntry.getKey().getId() );

                String newHostname = String.format( "%s-%d-%s", newHostnameEntry.getValue(),
                        environment.getPeerConf( environmentContainer.getPeerId() ).getVlan(),
                        StringUtils.substringAfterLast( environmentContainer.getIp(), "." ) );

                String oldHostname = environmentContainer.getHostname();

                environmentContainer.setHostname( newHostname );

                oldHostNames.put( newHostnameEntry.getKey(), oldHostname );
            }
            catch ( Exception e )
            {
                ok = false;

                trackerOperation.addLog( String.format( "Failed to change hostname of container %s: %s",
                        newHostnameEntry.getKey().getId(), e.getMessage() ) );
            }
        }

        //todo review may be we should complete the whole chain without throwing exception
        if ( !ok )
        {
            throw new PeerException( "Failed to change all containers' hostnames" );
        }

        return environmentManager.loadEnvironment( environment.getId() );
    }


    public Map<HostId, String> getOldHostNames()
    {
        return oldHostNames;
    }
}
