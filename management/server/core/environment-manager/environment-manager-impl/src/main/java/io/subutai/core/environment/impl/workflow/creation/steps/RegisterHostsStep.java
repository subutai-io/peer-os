package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Maps;

import io.subutai.common.environment.HostAddresses;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


public class RegisterHostsStep
{
    private final Topology topology;
    private final LocalEnvironment environment;
    private final TrackerOperation trackerOperation;
    PeerUtil<Object> hostUtil = new PeerUtil<>();


    public RegisterHostsStep( final Topology topology, final LocalEnvironment environment,
                              final TrackerOperation trackerOperation )
    {
        this.topology = topology;
        this.environment = environment;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentManagerException, PeerException
    {
        if ( !environment.getContainerHosts().isEmpty() && topology.registerHosts() )
        {
            registerHosts( environment.getContainerHosts() );
        }
    }


    void registerHosts( Set<EnvironmentContainerHost> hosts ) throws EnvironmentManagerException, PeerException
    {

        Set<Peer> peers = environment.getPeers();

        final Map<String, String> hostAddresses = Maps.newHashMap();

        for ( ContainerHost host : hosts )
        {
            hostAddresses.put( host.getHostname(), host.getIp() );
        }


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

        for ( PeerUtil.PeerTaskResult hostResult : hostResults.getResults() )
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
