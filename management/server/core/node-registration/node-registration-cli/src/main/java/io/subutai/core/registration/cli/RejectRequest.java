package io.subutai.core.registration.cli;


import java.util.UUID;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registration.api.RegistrationManager;


@Command( scope = "node", name = "reject", description = "approve new registration request" )
public class RejectRequest extends SubutaiShellCommandSupport
{

    @Argument( index = 0, name = "request Id", multiValued = false, required = true, description = "Request Id" )
    private String requestId;

    private RegistrationManager registrationManager;


    public RejectRequest( final RegistrationManager registrationManager )
    {
        this.registrationManager = registrationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        registrationManager.rejectRequest( requestId );

        System.out.println( registrationManager.getRequest( requestId ).toString() );

        return null;
    }
}
