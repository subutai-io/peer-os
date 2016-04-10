package io.subutai.core.environment.impl.workflow.modification.steps.helpers;


import org.apache.commons.lang3.exception.ExceptionUtils;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.common.util.TaskUtil;


public class ContainerDestroyTask extends TaskUtil.Task<Object>
{

    private final ContainerHost containerHost;


    public ContainerDestroyTask( final ContainerHost containerHost )
    {
        this.containerHost = containerHost;
    }


    public ContainerHost getContainerHost()
    {
        return containerHost;
    }


    @Override
    public Object call() throws Exception
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

        return null;
    }
}