package io.subutai.core.metric.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.metric.api.Monitor;


@Command( scope = "metric", name = "notify-listeners" )
public class NotifyListenersCommand extends SubutaiShellCommandSupport
{

    private Monitor monitor;


    public NotifyListenersCommand( final Monitor monitor )
    {
        this.monitor = monitor;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        monitor.notifyAlertListeners();
        return null;
    }
}
