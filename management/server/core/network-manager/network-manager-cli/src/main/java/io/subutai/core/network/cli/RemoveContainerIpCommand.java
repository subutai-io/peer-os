package io.subutai.core.network.cli;


import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


@Command( scope = "net", name = "remove-container-ip", description = "Removes container IP" )
public class RemoveContainerIpCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( RemoveContainerIpCommand.class.getName() );

    private final NetworkManager networkManager;

    @Argument( index = 0, name = "container name", required = true, multiValued = false,
            description = "container name" )
    String containerName;


    public RemoveContainerIpCommand( final NetworkManager networkManager )
    {
        Preconditions.checkNotNull( networkManager );

        this.networkManager = networkManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            networkManager.removeContainerIp( containerName );
            System.out.println( "OK" );
        }
        catch ( NetworkManagerException e )
        {
            System.out.println( e.getMessage() );
            LOG.error( "Error in RemoveContainerIpCommand", e );
        }

        return null;
    }
}
