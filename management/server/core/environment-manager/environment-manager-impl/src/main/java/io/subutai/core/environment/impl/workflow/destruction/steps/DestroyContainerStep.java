package io.subutai.core.environment.impl.workflow.destruction.steps;


import org.apache.commons.lang3.exception.ExceptionUtils;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class DestroyContainerStep
{
    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentImpl environment;
    private final ContainerHost containerHost;


    public DestroyContainerStep( final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
                                 final ContainerHost containerHost )
    {
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.containerHost = containerHost;
    }


    public void execute() throws PeerException
    {
        try
        {
            ( ( EnvironmentContainerImpl ) containerHost ).destroy();
        }
        catch ( PeerException e )
        {
            if ( !( e instanceof HostNotFoundException || ( ExceptionUtils.getRootCauseMessage( e )
                                                                          .contains( "HostNotFoundException" ) ) ) )
            {
                throw e;
            }
        }

        environment.removeContainer( containerHost );

        environmentManager.notifyOnContainerDestroyed( environment, containerHost.getId() );
    }
}
