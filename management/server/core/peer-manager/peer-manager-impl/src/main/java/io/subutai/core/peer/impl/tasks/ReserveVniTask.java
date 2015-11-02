package io.subutai.core.peer.impl.tasks;


import java.util.concurrent.Callable;

import io.subutai.common.network.Vni;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.impl.entity.ManagementHostEntity;


public class ReserveVniTask implements Callable<Vni>
{
    private final NetworkManager networkManager;
    private final Vni vni;
    private final ManagementHostEntity managementHost;


    public ReserveVniTask( final NetworkManager networkManager, final Vni vni,
                           final ManagementHostEntity managementHost )
    {
        this.networkManager = networkManager;
        this.vni = vni;
        this.managementHost = managementHost;
    }


    @Override
    public Vni call() throws Exception
    {

        //check if vni is already reserved
        Vni existingVni = managementHost.findVniByEnvironmentId( vni.getEnvironmentId() );
        if ( existingVni != null )
        {
            return existingVni;
        }

        //figure out available vlan
        int vlan = managementHost.findAvailableVlanId();

        //reserve vni & vlan for environment
        final Vni result = new Vni( this.vni.getVni(), vlan, this.vni.getEnvironmentId() );
        networkManager.reserveVni( result );

        return result;
    }
}
