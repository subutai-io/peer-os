package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.settings.Common;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "management-key-exchange" )
public class ManagementKeyExchangeCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;


    public ManagementKeyExchangeCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        localPeer.exchangeKeys( localPeer.getManagementHost(), Common.MANAGEMENT_HOSTNAME );

        System.out.println( "Keys are exchanged successfully" );

        return null;
    }
}
