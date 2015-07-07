package io.subutai.core.network.cli;


import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


@Command( scope = "net", name = "set-container-ip", description = "Sets container IP" )
public class SetContainerIpCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( SetContainerIpCommand.class.getName() );

    private final NetworkManager networkManager;

    @Argument( index = 0, name = "container name", required = true, multiValued = false,
            description = "container name" )
    String containerName;
    @Argument( index = 1, name = "ip", required = true, multiValued = false,
            description = "ip" )
    String ip;
    @Argument( index = 2, name = "net mask", required = true, multiValued = false,
            description = "net mask" )
    int netMask;
    @Argument( index = 3, name = "VLAN ID ", required = true, multiValued = false,
            description = "VLAN ID" )
    int vLanId;


    public SetContainerIpCommand( final NetworkManager networkManager )
    {
        Preconditions.checkNotNull( networkManager );

        this.networkManager = networkManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            networkManager.setContainerIp( containerName, ip, netMask, vLanId );
            System.out.println( "OK" );
        }
        catch ( NetworkManagerException e )
        {
            System.out.println( e.getMessage() );
            LOG.error( "Error in SetContainerIpCommand", e );
        }

        return null;
    }
}
