package io.subutai.core.environment.impl.workflow.modification.steps.helpers;


import java.util.concurrent.Callable;

import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.P2PConfig;


public class SetupP2PConnectionTask implements Callable<Object>
{
    private Peer peer;
    private P2PConfig p2PConfig;


    public SetupP2PConnectionTask( final Peer peer, final P2PConfig config )
    {
        this.peer = peer;
        this.p2PConfig = config;
    }


    @Override
    public P2PConfig call() throws Exception
    {
        peer.joinOrUpdateP2PSwarm( p2PConfig );

        return p2PConfig;
    }
}
