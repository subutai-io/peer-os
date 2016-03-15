package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registration.api.RegistrationManager;


@Command( scope = "localpeer", name = "management-key-exchange" )
public class ManagementKeyExchangeCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;
    private RegistrationManager registrationManager;


    public ManagementKeyExchangeCommand( final LocalPeer localPeer, final RegistrationManager registrationManager )
    {
        this.localPeer = localPeer;
        this.registrationManager = registrationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {


        String token = registrationManager.generateContainerTTLToken( 30 * 1000L ).getToken();

        final RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "subutai import management -t %s", token ) );

        CommandResult commandResult = localPeer.getManagementHost().execute( requestBuilder );

        System.out.println( commandResult.toString() );

        return null;
    }
}
