package io.subutai.core.registration.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registration.api.HostRegistrationManager;
import io.subutai.core.registration.api.exception.HostRegistrationException;


@Command( scope = "host", name = "verify", description = "Verifies container token" )
public class VerifyContainerToken extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "token", multiValued = false, required = true, description = "Token" )
    private String token;

    @Argument( index = 1, name = "token", multiValued = false, required = true, description = "Token" )
    private String containerHostId;

    @Argument( index = 2, name = "publicKey", multiValued = false, required = true, description = "Container public "
            + "key" )
    private String publicKey;

    private HostRegistrationManager registrationManager;


    public VerifyContainerToken( final HostRegistrationManager registrationManager )
    {
        this.registrationManager = registrationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        try
        {
            boolean valid = registrationManager.verifyTokenAndRegisterKey( token, containerHostId, publicKey );
            System.out.println( String.format( "Token valid =  %s", valid ) );
        }
        catch ( HostRegistrationException ex )
        {
            System.out.println( "Token verification failed." );
        }
        return null;
    }
}
