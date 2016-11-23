package io.subutai.core.environment.impl.workflow.modification.steps.helpers;


import org.apache.commons.lang3.StringUtils;

import io.subutai.common.host.HostId;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


public class RenameContainerTask extends TaskUtil.Task<Object>
{
    private final LocalEnvironment environment;
    private final HostId containerId;
    private final String newHostname;

    private String oldHostname;
    private String newFullHostname;


    public RenameContainerTask( final LocalEnvironment environment, final HostId containerId, final String newHostname )
    {
        this.environment = environment;
        this.containerId = containerId;
        this.newHostname = newHostname;
    }


    @Override
    public EnvironmentContainerImpl call() throws Exception
    {
        EnvironmentContainerImpl environmentContainer =
                ( EnvironmentContainerImpl ) environment.getContainerHostById( containerId.getId() );

        newFullHostname = String.format( "%s-%d-%s", newHostname.replaceAll( "\\s+", "" ),
                environment.getEnvironmentPeer( environmentContainer.getPeerId() ).getVlan(),
                StringUtils.substringAfterLast( environmentContainer.getIp(), "." ) );

        oldHostname = environmentContainer.getHostname();

        return ( EnvironmentContainerImpl ) environmentContainer.setHostname( newFullHostname );
    }


    public String getOldHostname()
    {
        return oldHostname;
    }


    public String getNewHostname()
    {
        return newFullHostname;
    }
}
