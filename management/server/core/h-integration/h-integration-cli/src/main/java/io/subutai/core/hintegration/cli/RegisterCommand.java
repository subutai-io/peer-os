package io.subutai.core.hintegration.cli;


import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import io.subutai.core.hintegration.api.Integration;


/**
 * HIntegration CLI implementation
 */

@Command( scope = "hint", name = "register" )
public class RegisterCommand extends OsgiCommandSupport
{
    private Integration integration;


    public RegisterCommand( final Integration integration )
    {
        this.integration = integration;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        System.out.println( "Registering..." );

        try
        {
            integration.register();
            System.out.println( "Peer registered successfully." );
        }
        catch ( Exception e )
        {
            log.debug( e.getMessage(), e );
            System.out.println( "Peer registration failed." );
        }
        return null;
    }
}
