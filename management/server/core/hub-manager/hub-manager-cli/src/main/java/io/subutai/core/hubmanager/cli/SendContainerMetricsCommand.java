package io.subutai.core.hubmanager.cli;


import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * Send container metrics command
 */
@Command( scope = "hub", name = "send-container-metrics", description = "Sends container metrics" )
public class SendContainerMetricsCommand extends SubutaiShellCommandSupport
{

    private final HubManager hubManager;


    public SendContainerMetricsCommand( final HubManager hubManager )
    {
        Preconditions.checkNotNull( hubManager, "Hub manager is null" );

        this.hubManager = hubManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        hubManager.sendContainerMertics();

        return null;
    }
}
