package io.subutai.core.hubmanager.impl.environment;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.settings.Common;


class P2PHelper extends Helper
{
    P2PHelper( LocalPeer localPeer )
    {
        super( localPeer );
    }


    @Override
    void execute( PeerEnvironmentDto dto ) throws Exception
    {
        runConnectionTask( localPeer, dto );

        setupTunnel( localPeer, dto );
    }


    private void setupTunnel( LocalPeer localPeer, PeerEnvironmentDto dto )
            throws InterruptedException, ExecutionException
    {
        log.debug( "P2P Tunnel Setup - START" );

        P2pIps p2pIps = new P2pIps();

        if ( p2pIps.isEmpty() )
        {
            return;
        }

        ExecutorService tunnelExecutor = Executors.newSingleThreadScheduledExecutor();

        ExecutorCompletionService<Boolean> tunnelCompletionService = new ExecutorCompletionService( tunnelExecutor );

        tunnelCompletionService.submit( new SetupTunnelTask( localPeer, dto.getEnvironmentId(), p2pIps ) );

        Future<Boolean> f2 = tunnelCompletionService.take();

        f2.get();

        tunnelExecutor.shutdown();

        log.debug( "P2P Tunnel Setup - END" );
    }


    private void runConnectionTask( LocalPeer localPeer, PeerEnvironmentDto dto )
            throws InterruptedException, ExecutionException
    {
        log.debug( "P2P Connection Task - START" );

        P2PConfig config = new P2PConfig( localPeer.getId(), dto.getEnvironmentId(), "com_" + dto.getEnvironmentId(),
                dto.getP2pIp(), dto.getP2pSharedKey(), Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );

        ExecutorService p2pExecutor = Executors.newSingleThreadScheduledExecutor();

        ExecutorCompletionService<P2PConfig> p2pCompletionService = new ExecutorCompletionService<>( p2pExecutor );

        p2pCompletionService.submit( new SetupP2PConnectionTask( localPeer, config ) );

        Future<P2PConfig> f = p2pCompletionService.take();

        f.get();

        p2pExecutor.shutdown();

        log.debug( "P2P Connection Task - END" );
    }


    private class SetupTunnelTask implements Callable<Boolean>
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
            peer.setupTunnels( p2pIps, environmentId );

            return true;
        }
    }


    private class SetupP2PConnectionTask implements Callable<P2PConfig>
    {
        private Peer peer;
        private P2PConfig p2PConfig;


        public SetupP2PConnectionTask( Peer peer, P2PConfig config )
        {
            this.peer = peer;
            this.p2PConfig = config;
        }


        @Override
        public P2PConfig call() throws Exception
        {
            peer.setupP2PConnection( p2PConfig );

            return p2PConfig;
        }
    }
}
