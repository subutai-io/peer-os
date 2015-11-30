package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.AlertPack;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "alert-listeners" )
public class AlertListenersCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;


    public AlertListenersCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        for ( AlertListener listener : localPeer.getAlertListeners() )
        {
            System.out.println( listener );
        }

        for ( AlertPack p: localPeer.getAlertPackages() )
        {
            System.out.println( p );
        }
        return null;
    }
}
