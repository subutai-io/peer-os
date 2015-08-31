package io.subutai.core.registration.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.service.ContainerToken;


/**
 * Created by talas on 8/28/15.
 */
@Command( scope = "node", name = "generate-token", description = "Generates container token" )
public class GenerateContainerToken extends SubutaiShellCommandSupport
{
    private RegistrationManager registrationManager;


    public GenerateContainerToken( final RegistrationManager registrationManager )
    {
        this.registrationManager = registrationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        ContainerToken containerToken = registrationManager.generateContainerTTLToken( 30 * 1000000L );
        System.out.println( containerToken.getToken() );
        return null;
    }
}
