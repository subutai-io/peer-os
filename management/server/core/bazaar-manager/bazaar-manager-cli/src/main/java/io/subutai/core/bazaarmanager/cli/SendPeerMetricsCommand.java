package io.subutai.core.bazaarmanager.cli;


import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.core.bazaarmanager.api.BazaarManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * Send peer metrics command
 */
@Command( scope = "bazaar", name = "send-peer-metrics", description = "Send peer metrics" )
public class SendPeerMetricsCommand extends SubutaiShellCommandSupport
{

    private final BazaarManager bazaarManager;


    public SendPeerMetricsCommand( final BazaarManager bazaarManager )
    {
        Preconditions.checkNotNull( bazaarManager, "Bazaar manager is null" );

        this.bazaarManager = bazaarManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        bazaarManager.sendPeersMertics();

        return null;
    }
}
