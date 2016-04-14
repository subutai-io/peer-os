package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "check-update-management" )
public class CheckUpdateManagementCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;


    public CheckUpdateManagementCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        final RequestBuilder requestBuilder = new RequestBuilder( "/opt/subutai-mng/bin/update-management --check" );

        CommandResult commandResult = localPeer.findHostByName( "management" ).execute( requestBuilder );

        System.out.println( commandResult.toString() );
        return null;
    }
}
