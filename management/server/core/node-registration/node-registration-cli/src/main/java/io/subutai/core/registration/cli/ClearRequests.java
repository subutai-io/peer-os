package io.subutai.core.registration.cli;


import java.util.List;
import java.util.UUID;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.service.RequestedHost;


@Command( scope = "node", name = "clear-requests", description = "approve new registration request" )
public class ClearRequests extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "request status", multiValued = false, required = true, description = "request "
            + "status" )
    private String status;

    private RegistrationManager registrationManager;


    public ClearRequests( final RegistrationManager registrationManager )
    {
        this.registrationManager = registrationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        List<RequestedHost> requestedHosts = registrationManager.getRequests();
        for ( final RequestedHost requestedHost : requestedHosts )
        {
            System.out.println( requestedHost.toString() );
            System.out.println( "==========" );
            //            if ( status.equalsIgnoreCase( requestedHost.getState().name() ) || )
            //            {
            registrationManager.removeRequest(requestedHost.getId());
            //            }
        }
        return null;
    }
}
