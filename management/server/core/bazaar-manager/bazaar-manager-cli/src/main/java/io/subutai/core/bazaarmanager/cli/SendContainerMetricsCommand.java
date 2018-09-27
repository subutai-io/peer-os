package io.subutai.core.bazaarmanager.cli;


import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.core.bazaarmanager.api.BazaarManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * Send container metrics command
 */
@Command( scope = "bazaar", name = "send-container-metrics", description = "Sends container metrics" )
public class SendContainerMetricsCommand extends SubutaiShellCommandSupport
{

    private final BazaarManager bazaarManager;


    public SendContainerMetricsCommand( final BazaarManager bazaarManager )
    {
        Preconditions.checkNotNull( bazaarManager, "Bazaar manager is null" );

        this.bazaarManager = bazaarManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        bazaarManager.sendContainerMertics();

        return null;
    }
}
