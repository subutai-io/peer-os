package io.subutai.core.environment.impl.workflow.modification.steps.helpers;


import io.subutai.common.host.HostId;
import io.subutai.common.util.StringUtil;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


public class RenameContainerTask extends TaskUtil.Task<Object>
{
    private final LocalEnvironment environment;
    private final HostId containerId;
    private String newHostname;

    private String oldHostname;


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

        oldHostname = environmentContainer.getHostname();

        newHostname = StringUtil.removeHtmlAndSpecialChars( newHostname, true );

        return ( EnvironmentContainerImpl ) environmentContainer.setHostname( newHostname, false );
    }


    public String getOldHostname()
    {
        return oldHostname;
    }


    public String getNewHostname()
    {
        return newHostname;
    }
}
