package io.subutai.core.localpeer.impl.tasks;


import java.util.Set;
import java.util.concurrent.Callable;

import io.subutai.common.network.Gateway;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;


public class CreateGatewayTask implements Callable<Boolean>
{
    private final Gateway gateway;
    private final NetworkManager networkManager;
    private final LocalPeer localPeer;


    public CreateGatewayTask( final Gateway gateway, final NetworkManager networkManager,
                              final LocalPeer localPeer )
    {
        this.gateway = gateway;
        this.networkManager = networkManager;
        this.localPeer = localPeer;
    }


    @Override
    public Boolean call() throws Exception
    {

        Gateway newGateway = new Gateway( gateway.getVlan(), gateway.getIp() );

        try
        {
            Set<Gateway> existingGateways = localPeer.getGateways();
            for ( Gateway gateway : existingGateways )
            {
                if ( newGateway.equals( gateway ) )
                {
                    return false;
                }
            }

            networkManager.setupGateway( gateway.getIp(), gateway.getVlan() );

            return true;
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException(
                    String.format( "Error creating gateway tap device with IP %s and VLAN %d", gateway.getIp(), gateway.getVlan() ), e );
        }
    }
}
