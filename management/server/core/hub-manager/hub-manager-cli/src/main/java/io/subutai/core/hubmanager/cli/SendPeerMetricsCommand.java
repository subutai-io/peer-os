package io.subutai.core.hubmanager.cli;


import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * Send peer metrics command
 */
@Command( scope = "hub", name = "send-peer-metrics", description = "Send peer metrics" )
public class SendPeerMetricsCommand extends SubutaiShellCommandSupport
{

    private final HubManager hubManager;


    public SendPeerMetricsCommand( final HubManager hubManager )
    {
        Preconditions.checkNotNull( hubManager, "Hubmanager is null" );

        this.hubManager = hubManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        hubManager.sendPeersMertics();

        return null;
    }
}
