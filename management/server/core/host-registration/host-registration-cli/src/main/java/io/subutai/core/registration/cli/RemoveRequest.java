package io.subutai.core.registration.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registration.api.HostRegistrationManager;


@Command( scope = "host", name = "remove", description = "removes registration request" )
public class RemoveRequest extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "requestId", multiValued = false, required = true, description = "Request Id" )
    private String requestId;

    private HostRegistrationManager registrationManager;


    public RemoveRequest( final HostRegistrationManager registrationManager )
    {
        this.registrationManager = registrationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        registrationManager.removeRequest( requestId );

        System.out.println( "Request removed");

        return null;
    }
}
