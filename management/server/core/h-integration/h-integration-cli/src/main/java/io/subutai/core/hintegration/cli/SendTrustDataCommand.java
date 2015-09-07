package io.subutai.core.hintegration.cli;


import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import io.subutai.core.hintegration.api.Integration;


/**
 * HIntegration CLI implementation
 */

@Command( scope = "hint", name = "send-trust-data" )
public class SendTrustDataCommand extends OsgiCommandSupport
{
    private Integration integration;


    public SendTrustDataCommand( final Integration integration )
    {
        this.integration = integration;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        System.out.println( "Sending trust data..." );

        try
        {
            integration.sendTrustData();
            System.out.println( "Trust data sent successfully." );
        }
        catch ( Exception e )
        {
            log.debug( e.getMessage(), e );
            System.out.println( "Trust data sending failed." );
        }
        return null;
    }
}
