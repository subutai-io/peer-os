package org.safehaus.subutai.core.network.cli;


import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


@Command( scope = "net", name = "setup-n2n", description = "Sets up N2N connection with Hub" )
public class SetupN2NCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( SetupN2NCommand.class.getName() );

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


    public SetupN2NCommand( final NetworkManager networkManager )
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
                    .setupN2NConnection( superNodeIp, superNodePort, interfaceName, communityName, localIp, keyType,
                            pathToKeyFile );
            System.out.println( "OK" );
        }
        catch ( NetworkManagerException e )
        {
            System.out.println( e.getMessage() );
            LOG.error( "Error in SetupN2NCommand", e );
        }

        return null;
    }
}
