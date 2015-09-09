package io.subutai.core.hintegration.cli;


import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import io.subutai.core.hintegration.api.Integration;


/**
 * HIntegration CLI implementation
 */

@Command( scope = "hint", name = "send-heartbeat" )
public class SendHearbeatCommand extends OsgiCommandSupport
{
    private Integration integration;


    public SendHearbeatCommand( final Integration integration )
    {
        this.integration = integration;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        System.out.println( "Sending hearbeat..." );

        try
        {
            integration.sendHeartbeat();
            System.out.println( "Heartbeat sent successfully." );
        }
        catch ( Exception e )
        {
            log.debug( e.getMessage(), e );
            System.out.println( "Heartbeat sending failed." );
        }
        return null;
    }
}
