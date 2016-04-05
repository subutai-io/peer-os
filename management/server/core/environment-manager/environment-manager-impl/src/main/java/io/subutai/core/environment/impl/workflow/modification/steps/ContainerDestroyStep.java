package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


/**
 * Container destroy step
 */
public class ContainerDestroyStep
{
    private final EnvironmentImpl environment;
    private final EnvironmentManagerImpl environmentManager;
    private final List<String> removedContainers;
    private final boolean forceMetadataRemoval;
    private final TrackerOperation operationTracker;


    public ContainerDestroyStep( final EnvironmentImpl environment, final EnvironmentManagerImpl environmentManager,
                                 final List<String> removedContainers, final boolean forceMetadataRemoval,
                                 final TrackerOperation operationTracker )
    {
        this.environment = environment;
        this.environmentManager = environmentManager;
        this.removedContainers = removedContainers;
        this.forceMetadataRemoval = forceMetadataRemoval;
        this.operationTracker = operationTracker;
    }


    public void execute() throws Exception
    {
        for ( String containerId : removedContainers )
        {
            ContainerHost containerHost;

            containerHost = environment.getContainerHostById( containerId );

            try
            {
                ( ( EnvironmentContainerImpl ) containerHost ).destroy();
            }
            catch ( PeerException e )
            {
                boolean skipError = false;
                if ( e instanceof HostNotFoundException || ( ExceptionUtils.getRootCauseMessage( e )
                                                                           .contains( "HostNotFoundException" ) ) )
                {
                    //skip error since host is not found
                    skipError = true;
                }
                if ( !skipError )
                {
                    if ( forceMetadataRemoval )
                    {
                        operationTracker.addLog( String.format( "Error destroying container: %s", e.getMessage() ) );
                    }
                    else
                    {
                        throw e;
                    }
                }
            }


            environment.removeContainer( containerHost );
            environmentManager.notifyOnContainerDestroyed( environment, containerHost.getId() );
        }
    }
}
