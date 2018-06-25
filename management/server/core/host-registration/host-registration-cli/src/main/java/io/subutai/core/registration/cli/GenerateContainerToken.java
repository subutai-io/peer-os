package io.subutai.core.registration.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registration.api.HostRegistrationManager;


@Command( scope = "host", name = "generate-token", description = "Generates container token" )
public class GenerateContainerToken extends SubutaiShellCommandSupport
{
    private HostRegistrationManager registrationManager;


    public GenerateContainerToken( final HostRegistrationManager registrationManager )
    {
        this.registrationManager = registrationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        String containerToken = registrationManager.generateContainerToken( 30 * 1000000L );
        System.out.println( containerToken );
        return null;
    }
}
