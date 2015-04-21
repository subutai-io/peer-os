package org.safehaus.subutai.core.peer.impl.tasks;


import java.util.Set;
import java.util.concurrent.Callable;

import org.safehaus.subutai.common.network.Gateway;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.peer.api.ManagementHost;


public class CreateGatewayTask implements Callable<Boolean>
{
    private final String gatewayIp;
    private final int vlan;
    private final NetworkManager networkManager;
    private final ManagementHost managementHost;


    public CreateGatewayTask( final String gatewayIp, final int vlan, final NetworkManager networkManager,
                              final ManagementHost managementHost )
    {
        this.gatewayIp = gatewayIp;
        this.vlan = vlan;
        this.networkManager = networkManager;
        this.managementHost = managementHost;
    }


    @Override
    public Boolean call() throws Exception
    {

        Gateway newGateway = new Gateway( vlan, gatewayIp );

        try
        {
            Set<Gateway> existingGateways = managementHost.getGateways();
            for ( Gateway gateway : existingGateways )
            {
                if ( gateway.equals( newGateway ) )
                {
                    return false;
                }
            }

            networkManager.setupGateway( gatewayIp, vlan );

            return true;
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException(
                    String.format( "Error creating gateway tap device with IP %s and VLAN %d", gatewayIp, vlan ), e );
        }
    }
}
