package io.subutai.core.network.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;


@Command( scope = "net", name = "setup-p2p", description = "Sets up P2P connection with control peer" )
public class SetupP2PCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( SetupP2PCommand.class.getName() );

    private final NetworkManager networkManager;

    @Argument( index = 0, name = "super node ip", required = true, multiValued = false,
            description = "super node ip" )
    String superNodeIp;
    @Argument( index = 1, name = "super node port", required = true, multiValued = false,
            description = "super node port" )
    int superNodePort;
    @Argument( index = 2, name = "interface name", required = true, multiValued = false,
            description = "interface name" )
    String interfaceName;
    @Argument( index = 3, name = "community name", required = true, multiValued = false,
            description = "community name" )
    String communityName;
    @Argument( index = 4, name = "local peer IP", required = true, multiValued = false,
            description = "local peer IP" )
    String localIp;
    @Argument( index = 5, name = "key type", required = true, multiValued = false,
            description = "type of key" )
    String keyType;
    @Argument( index = 6, name = "key file path", required = true, multiValued = false,
            description = "path to key file" )
    String pathToKeyFile;


    public SetupP2PCommand( final NetworkManager networkManager )
    {
        Preconditions.checkNotNull( networkManager );

        this.networkManager = networkManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            networkManager
                    .setupP2PConnection( superNodeIp, superNodePort, interfaceName, communityName, localIp, keyType,
                            pathToKeyFile );
            System.out.println( "OK" );
        }
        catch ( NetworkManagerException e )
        {
            System.out.println( e.getMessage() );
            LOG.error( "Error in SetupP2PCommand", e );
        }

        return null;
    }
}
