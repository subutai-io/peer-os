package io.subutai.core.hostregistry.impl;


import com.google.common.base.Preconditions;

import io.subutai.common.host.HeartBeat;
import io.subutai.common.host.HeartbeatListener;


public class HeartbeatProcessor implements HeartbeatListener
{
    private final HostRegistryImpl registry;


    public HeartbeatProcessor( final HostRegistryImpl registry )
    {
        Preconditions.checkNotNull( registry );

        this.registry = registry;
    }


    @Override
    public void onHeartbeat( final HeartBeat heartBeat )
    {
        registry.registerHost( heartBeat.getHostInfo(), heartBeat.getAlerts() );
    }
}
