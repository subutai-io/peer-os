package io.subutai.core.metric.cli;


import java.util.Collection;

import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.AlertPack;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.metric.api.Monitor;


/**
 * Queue of alerts command
 */
@Command( scope = "alert", name = "queue", description = "Returns local alerts in queue" )
public class QueueListCommand extends SubutaiShellCommandSupport
{
    private final Monitor monitor;


    public QueueListCommand( final Monitor monitor )
    {
        Preconditions.checkNotNull( monitor, "Monitor is null" );

        this.monitor = monitor;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Collection<AlertPack> alerts = monitor.getAlerts();
        System.out.println( "List of alerts in queue:" );
        for ( AlertPack alert : monitor.getAlertsQueue() )
        {
            System.out.println( alert );
        }

        return null;
    }
}
