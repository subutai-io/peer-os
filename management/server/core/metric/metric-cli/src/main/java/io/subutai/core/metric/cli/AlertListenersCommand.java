package io.subutai.core.metric.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.AlertPack;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.metric.api.Monitor;


@Command( scope = "metric", name = "alert-listeners" )
public class AlertListenersCommand extends SubutaiShellCommandSupport
{

    private Monitor monitor;


    public AlertListenersCommand( final Monitor monitor )
    {
        this.monitor = monitor;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        for ( AlertListener listener : monitor.getAlertListeners() )
        {
            System.out.println( listener );
        }

        for ( AlertPack p : monitor.getAlertPackages() )
        {
            System.out.println( p );
        }
        return null;
    }
}
