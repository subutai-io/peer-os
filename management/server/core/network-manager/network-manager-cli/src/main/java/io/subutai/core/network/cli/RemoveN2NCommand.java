package io.subutai.core.network.cli;


import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


@Command( scope = "net", name = "remove-n2n", description = "Removes N2N connection" )
public class RemoveN2NCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( RemoveN2NCommand.class.getName() );

    private final NetworkManager networkManager;

    @Argument( index = 0, name = "interface name", required = true, multiValued = false,
            description = "interface name" )
    String interfaceName;
    @Argument( index = 1, name = "community name", required = true, multiValued = false,
            description = "community name" )
    String communityName;


    public RemoveN2NCommand( final NetworkManager networkManager )
    {
        Preconditions.checkNotNull( networkManager );

        this.networkManager = networkManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            networkManager.removeN2NConnection( interfaceName, communityName );
            System.out.println( "OK" );
        }
        catch ( NetworkManagerException e )
        {
            System.out.println( e.getMessage() );
            LOG.error( "Error in RemoveN2NCommand", e );
        }

        return null;
    }
}
