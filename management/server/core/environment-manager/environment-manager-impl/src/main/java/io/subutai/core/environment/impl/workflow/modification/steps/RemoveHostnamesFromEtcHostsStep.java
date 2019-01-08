package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Set;
import java.util.concurrent.Callable;

import io.subutai.common.environment.HostAddresses;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.entity.LocalEnvironment;

//todo use in ContainerDestructionWorkflow and EnvironmentModifyWorkflow
public class RemoveHostnamesFromEtcHostsStep
{
    private final TrackerOperation trackerOperation;
    private final LocalEnvironment environment;
    private final Set<ContainerHost> removedContainers;
    protected PeerUtil<Object> peerUtil = new PeerUtil<>();


    public RemoveHostnamesFromEtcHostsStep( final LocalEnvironment environment,
                                            final Set<ContainerHost> removedContainers,
                                            final TrackerOperation trackerOperation )
    {
        this.environment = environment;
        this.removedContainers = removedContainers;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws PeerException
    {
        final HostAddresses hostAddresses = new HostAddresses( removedContainers );

        Set<Peer> peers = environment.getPeers();

        for ( final Peer peer : peers )
        {
            peerUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    peer.removeHostnamesFromEtcHosts( environment.getEnvironmentId(), hostAddresses );

                    return null;
                }
            } ) );
        }

        PeerUtil.PeerTaskResults<Object> peerResults = peerUtil.executeParallel();


        for ( PeerUtil.PeerTaskResult peerResult : peerResults.getResults() )
        {
            if ( peerResult.hasSucceeded() )
            {
                trackerOperation
                        .addLog( String.format( "Removed hostnames on peer %s", peerResult.getPeer().getName() ) );
            }
            else
            {
                trackerOperation.addLog( String.format( "Failed to remove hostnames on peer %s. Reason: %s",
                        peerResult.getPeer().getName(), peerResult.getFailureReason() ) );
            }
        }
    }
}
