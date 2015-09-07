package io.subutai.core.hintegration.cli;


import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import io.subutai.core.hintegration.api.Integration;


/**
 * HIntegration CLI implementation
 */

@Command( scope = "hint", name = "register-keys" )
public class RegisterKeysCommand extends OsgiCommandSupport
{
    private Integration integration;


    public RegisterKeysCommand( final Integration integration )
    {
        this.integration = integration;
    }

    @Override
    protected Object doExecute() throws Exception
    {
        System.out.println( "Keys registration..." );

        try
        {
            integration.registerOwnerPubKey();
            System.out.println( "Owner key registration succeed." );
        }
        catch ( Exception e )
        {
            System.out.println( "Owner key registration failed." );
        }
        try
        {
            integration.registerPeerPubKey();
            System.out.println( "Peer key registration succeed." );
        }
        catch ( Exception e )
        {
            System.out.println( "Peer key registration failed." );
        }
        return null;
    }
}
