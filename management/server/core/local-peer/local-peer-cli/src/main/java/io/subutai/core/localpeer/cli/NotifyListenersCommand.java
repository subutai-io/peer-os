package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "notify-listeners" )
public class NotifyListenersCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;


    public NotifyListenersCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        localPeer.notifyAlertListeners();
        return null;
    }
}
