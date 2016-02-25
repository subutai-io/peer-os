package io.subutai.core.localpeer.impl.tasks;


import java.util.SortedSet;
import java.util.concurrent.Callable;

import com.google.common.collect.Sets;

import io.subutai.common.network.Vni;
import io.subutai.common.network.Vnis;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.core.network.api.NetworkManager;


public class ReserveVniTask implements Callable<Vni>
{
    private final NetworkManager networkManager;
    private final Vni vni;
    private final LocalPeer localPeer;


    public ReserveVniTask( final NetworkManager networkManager, final Vni vni, final LocalPeer localPeer )
    {
        this.networkManager = networkManager;
        this.vni = vni;
        this.localPeer = localPeer;
    }


    @Override
    public Vni call() throws Exception
    {

        //check if vni is already reserved
        final Vnis reservedVnis = localPeer.getReservedVnis();
        Vni existingVni = reservedVnis.findVniByEnvironmentId( vni.getEnvironmentId() );
        if ( existingVni != null )
        {
            return existingVni;
        }

        //figure out available vlan
        int vlan = reservedVnis.findAvailableVlanId();

        //reserve vni & vlan for environment
        final Vni result = new Vni( this.vni.getVni(), vlan, this.vni.getEnvironmentId() );
        networkManager.reserveVni( result );

        return result;
    }
}
