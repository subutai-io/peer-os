package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import java.util.concurrent.Callable;

import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.P2pIps;


public class SetupTunnelTask implements Callable<Object>
{
    private final Peer peer;
    private final String environmentId;
    private final P2pIps p2pIps;


    public SetupTunnelTask( final Peer peer, final String environmentId, final P2pIps p2pIps )
    {
        this.peer = peer;
        this.environmentId = environmentId;
        this.p2pIps = p2pIps;
    }


    @Override
    public Boolean call() throws Exception
    {
        peer.setupTunnels( p2pIps, new EnvironmentId( environmentId ) );

        return true;
    }
}
