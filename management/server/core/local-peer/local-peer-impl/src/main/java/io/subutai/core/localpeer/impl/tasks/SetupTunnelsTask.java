package io.subutai.core.localpeer.impl.tasks;


import com.google.common.base.Preconditions;

import io.subutai.common.network.NetworkResource;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.util.HostUtil;


public class SetupTunnelsTask extends HostUtil.Task<Object>
{
    private final ResourceHost resourceHost;
    private final P2pIps p2pIps;
    private final NetworkResource networkResource;


    public SetupTunnelsTask( final ResourceHost resourceHost, final P2pIps p2pIps, final NetworkResource networkResource )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkNotNull( p2pIps );
        Preconditions.checkNotNull( networkResource );

        this.resourceHost = resourceHost;
        this.p2pIps = p2pIps;
        this.networkResource = networkResource;
    }


    @Override
    public int maxParallelTasks()
    {
        return 1;
    }


    @Override
    public String name()
    {
        return String.format( "Setup tunnels for environment %s", networkResource.getEnvironmentId() );
    }


    @Override
    public Object call() throws Exception
    {
        resourceHost.setupTunnels( p2pIps, networkResource );

        return null;
    }
}
