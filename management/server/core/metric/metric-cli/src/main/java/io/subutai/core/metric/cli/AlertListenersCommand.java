package io.subutai.core.metric.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.AlertHandler;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.metric.api.Monitor;


@Command( scope = "alert", name = "handlers" )
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
        System.out.println( "List of alert handlers:" );
        for ( AlertHandler listener : monitor.getAlertHandlers() )
        {
            System.out.println( listener.getHandlerId() );
        }

        return null;
    }
}
