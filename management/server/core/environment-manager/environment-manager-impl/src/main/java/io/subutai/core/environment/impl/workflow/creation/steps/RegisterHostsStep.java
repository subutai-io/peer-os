package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.HostAddresses;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class RegisterHostsStep
{
    private static final Logger LOG = LoggerFactory.getLogger( RegisterHostsStep.class );

    private final EnvironmentImpl environment;
    private final TrackerOperation trackerOperation;


    public RegisterHostsStep( final EnvironmentImpl environment, final TrackerOperation trackerOperation )
    {
        this.environment = environment;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentManagerException, PeerException
    {
        Set<Host> hosts = Sets.newHashSet();
        hosts.addAll( environment.getContainerHosts() );
        if ( hosts.size() > 1 )
        {
            registerHosts( hosts );
        }
    }


    protected void registerHosts( Set<Host> hosts ) throws EnvironmentManagerException, PeerException
    {

        Set<Peer> peers = environment.getPeers();

        ExecutorService executorService = Executors.newFixedThreadPool( peers.size() );
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        final Map<String, String> hostAddresses = Maps.newHashMap();

        for ( Host host : hosts )
        {
            hostAddresses
                    .put( host.getHostname(), host.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp() );
        }

        for ( final Peer peer : peers )
        {
            completionService.submit( new Callable<Peer>()
            {
                @Override
                public Peer call() throws Exception
                {
                    peer.configureHostsInEnvironment( environment.getEnvironmentId(),
                            new HostAddresses( hostAddresses ) );
                    return peer;
                }
            } );
        }

        Set<Peer> succeededPeers = Sets.newHashSet();
        for ( Peer ignored : peers )
        {
            try
            {
                Future<Peer> f = completionService.take();
                succeededPeers.add( f.get() );
            }
            catch ( Exception e )
            {
                LOG.error( "Problems registering hosts in environment", e );
            }
        }

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation.addLog( String.format( "Registered hosts on peer %s", succeededPeer.getName() ) );
        }

        Set<Peer> failedPeers = Sets.newHashSet( peers );
        failedPeers.removeAll( succeededPeers );

        for ( Peer failedPeer : failedPeers )
        {
            trackerOperation.addLog( String.format( "Failed to register hosts on peer %s", failedPeer.getName() ) );
        }

        if ( !failedPeers.isEmpty() )
        {
            throw new EnvironmentManagerException( "Failed to register hosts on all peers" );
        }
    }
}
