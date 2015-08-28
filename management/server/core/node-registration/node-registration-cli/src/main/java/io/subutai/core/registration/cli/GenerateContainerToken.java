package io.subutai.core.registration.cli;


import org.apache.karaf.shell.commands.Argument;
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
    @Argument( index = 0, name = "container host id", multiValued = false, required = true, description =
            "ContainerHost Id" )
    String containerHostId;

    private RegistrationManager registrationManager;


    public GenerateContainerToken( final RegistrationManager registrationManager )
    {
        this.registrationManager = registrationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        ContainerToken containerToken = registrationManager.generateContainerTTLToken( containerHostId, 30 * 1000L );
        System.out.println( containerToken.getToken() );
        return null;
    }
}
