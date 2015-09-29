package io.subutai.core.environment.impl.workflow.destruction.steps;


import org.apache.commons.lang3.exception.ExceptionUtils;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.common.peer.HostNotFoundException;


public class DestroyContainerStep
{
    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentImpl environment;
    private final ContainerHost containerHost;
    private final boolean forceMetadataRemoval;
    private final TrackerOperation operationTracker;


    public DestroyContainerStep( final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
                                 final ContainerHost containerHost, final boolean forceMetadataRemoval,
                                 final TrackerOperation operationTracker )
    {
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.containerHost = containerHost;
        this.forceMetadataRemoval = forceMetadataRemoval;
        this.operationTracker = operationTracker;
    }


    public void execute() throws PeerException
    {
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

        environment.removeContainer( containerHost.getId() );

        environmentManager.notifyOnContainerDestroyed( environment, containerHost.getId() );
    }
}
