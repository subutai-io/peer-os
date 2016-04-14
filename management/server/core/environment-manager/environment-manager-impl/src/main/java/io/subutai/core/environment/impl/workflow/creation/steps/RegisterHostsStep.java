package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.HostAddresses;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class RegisterHostsStep
{
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

        final Map<String, String> hostAddresses = Maps.newHashMap();

        for ( Host host : hosts )
        {
            hostAddresses
                    .put( host.getHostname(), host.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp() );
        }

        PeerUtil<Object> hostUtil = new PeerUtil<>();

        for ( final Peer peer : peers )
        {
            hostUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    peer.configureHostsInEnvironment( environment.getEnvironmentId(),
                            new HostAddresses( hostAddresses ) );

                    return null;
                }
            } ) );
        }

        PeerUtil.PeerTaskResults<Object> hostResults = hostUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult hostResult : hostResults.getPeerTaskResults() )
        {

            if ( hostResult.hasSucceeded() )
            {
                trackerOperation
                        .addLog( String.format( "Registered hosts on peer %s", hostResult.getPeer().getName() ) );
            }
            else
            {
                trackerOperation.addLog( String.format( "Failed to register hosts on peer %s. Reason: %s",
                        hostResult.getPeer().getName(), hostResult.getFailureReason() ) );
            }
        }

        if ( hostResults.hasFailures() )
        {
            throw new EnvironmentManagerException( "Failed to register hosts on all peers" );
        }
    }
}
