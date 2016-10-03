package io.subutai.core.registration.cli;


import java.util.List;

import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registration.api.HostRegistrationManager;
import io.subutai.core.registration.api.service.RequestedHost;


@Command( scope = "node", name = "clear-requests", description = "clear all request" )
public class ClearRequests extends SubutaiShellCommandSupport
{
    private HostRegistrationManager registrationManager;


    public ClearRequests( final HostRegistrationManager registrationManager )
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

            try
            {
                registrationManager.removeRequest( requestedHost.getId() );
            }
            catch ( Exception e )
            {
                System.err.format( "Error unregistering host %s: %s%n", requestedHost.getHostname(), e.getMessage() );
            }
        }

        return null;
    }
}
