package io.subutai.core.metric.cli;


import java.util.Collection;

import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.AlertPack;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.metric.api.Monitor;


/**
 * List of alerts command
 */
@Command( scope = "metric", name = "alerts", description = "Returns list of alerts" )
public class AlertListCommand extends SubutaiShellCommandSupport
{
    private final Monitor monitor;


    public AlertListCommand( final Monitor monitor )
    {
        Preconditions.checkNotNull( monitor, "Monitor is null" );

        this.monitor = monitor;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Collection<AlertPack> alerts = monitor.getAlerts();
        System.out.println( "List of alerts:" );
        for ( AlertPack alert : alerts )
        {
            System.out.println( alert );
        }

        return null;
    }
}
