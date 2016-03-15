package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "management-key-exchange" )
public class ManagementKeyExchangeCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;
    private IdentityManager identityManager;


    public ManagementKeyExchangeCommand( final LocalPeer localPeer, final IdentityManager identityManager )
    {
        this.localPeer = localPeer;
        this.identityManager = identityManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        String token = identityManager.getUserToken( "admin", "secret" );

        final RequestBuilder requestBuilder = new RequestBuilder( "subutai import management -t " + token );

        CommandResult commandResult = localPeer.getManagementHost().execute( requestBuilder );

        System.out.println( commandResult.toString() );
        return null;
    }
}
