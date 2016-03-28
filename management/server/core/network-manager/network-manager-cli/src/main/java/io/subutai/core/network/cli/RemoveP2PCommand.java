package io.subutai.core.network.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;


@Command( scope = "net", name = "p2p-remove", description = "Removes P2P connection" )
public class RemoveP2PCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( RemoveP2PCommand.class.getName() );

    private final NetworkManager networkManager;

    @Argument( index = 0, name = "p2p hash", required = true, multiValued = false,
            description = "p2p hash" )
    String p2pHash;


    public RemoveP2PCommand( final NetworkManager networkManager )
    {
        Preconditions.checkNotNull( networkManager );

        this.networkManager = networkManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            networkManager.removeP2PConnection( p2pHash );
            System.out.println( "OK" );
        }
        catch ( NetworkManagerException e )
        {
            System.out.println( e.getMessage() );
            LOG.error( "Error in RemoveP2PCommand", e );
        }

        return null;
    }
}
