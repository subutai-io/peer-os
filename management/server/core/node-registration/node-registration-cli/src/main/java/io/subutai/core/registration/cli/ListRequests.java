package io.subutai.core.registration.cli;


import java.util.List;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.service.RequestedHost;


@Command( scope = "node", name = "list", description = "approve new registration request" )
public class ListRequests extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "fullInfo", multiValued = false, required = false, description = "Request full "
            + "Description" )
    private boolean fullDescription;
    private RegistrationManager registrationManager;


    public ListRequests( final RegistrationManager registrationManager )
    {
        this.registrationManager = registrationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        List<RequestedHost> requestedHosts = registrationManager.getRequests();
        for ( final RequestedHost requestedHost : requestedHosts )
        {
            if ( fullDescription )
            {
                System.out.println( requestedHost.toString() );
            }
            else
            {
                System.out.print( requestedHost.getId() + "; " );
                System.out.print( requestedHost.getStatus() + "; " );
                System.out.print( requestedHost.getHostname() + "; " );
                System.out.print( requestedHost.getSecret() + "; " );
                System.out.print( requestedHost.getArch() + "; " );
                System.out.println( requestedHost.getRestHook() );
            }
            System.out.println( "==========" );
        }
        return null;
    }
}
