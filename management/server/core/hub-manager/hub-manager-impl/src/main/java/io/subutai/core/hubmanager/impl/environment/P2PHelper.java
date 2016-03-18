package io.subutai.core.hubmanager.impl.environment;


import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.collect.ImmutableMap;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.P2PConfig;
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


    private void setupTunnel( LocalPeer localPeer, PeerEnvironmentDto dto ) throws InterruptedException, ExecutionException
    {
        log.debug( "P2P Tunnel Setup - START");

        Map<String, String> tunnels = ImmutableMap.of( localPeer.getId(), dto.getP2pIp() );

        ExecutorService tunnelExecutor = Executors.newSingleThreadScheduledExecutor();

        ExecutorCompletionService<Integer> tunnelCompletionService = new ExecutorCompletionService( tunnelExecutor );

        tunnelCompletionService.submit( new SetupTunnelTask( localPeer, dto.getEnvironmentId(), tunnels ) );

        Future<Integer> f2 = tunnelCompletionService.take();

        Integer vlanid = f2.get();

        log.debug( "vlanid: {}", vlanid );

        tunnelExecutor.shutdown();

        log.debug( "P2P Tunnel Setup - END");
    }


    private void runConnectionTask( LocalPeer localPeer, PeerEnvironmentDto dto ) throws InterruptedException, ExecutionException
    {
        log.debug( "P2P Connection Task - START");

        P2PConfig config = new P2PConfig(
                localPeer.getId(),
                dto.getEnvironmentId(),
                "p2p_" + dto.getP2pSubnet(),
                "com_" + dto.getEnvironmentId(),
                dto.getP2pIp(),
                dto.getP2pSharedKey(),
                Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC
        );

        ExecutorService p2pExecutor = Executors.newSingleThreadScheduledExecutor();

        ExecutorCompletionService<P2PConfig> p2pCompletionService = new ExecutorCompletionService<>( p2pExecutor );

        p2pCompletionService.submit( new SetupP2PConnectionTask( localPeer, config ) );

        Future<P2PConfig> f = p2pCompletionService.take();

        f.get();

        p2pExecutor.shutdown();

        log.debug( "P2P Connection Task - END");
    }


    private class SetupTunnelTask implements Callable<Integer>
    {
        private final Peer peer;
        private final String environmentId;
        private final Map<String, String> tunnels;


        public SetupTunnelTask( final Peer peer, final String environmentId, final Map<String, String> tunnels )
        {
            this.peer = peer;
            this.environmentId = environmentId;
            this.tunnels = tunnels;
        }


        @Override
        public Integer call() throws Exception
        {
            return peer.setupTunnels( tunnels, environmentId );
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
