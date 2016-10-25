package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Set;
import java.util.concurrent.Callable;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class UpdateAuthorizedKeysStep
{
    private final EnvironmentImpl environment;
    private final String oldHostname;
    private final String newHostname;
    private final TrackerOperation trackerOperation;
    protected PeerUtil<Object> peerUtil = new PeerUtil<>();


    public UpdateAuthorizedKeysStep( final EnvironmentImpl environment, final String oldHostname,
                                     final String newHostname, TrackerOperation trackerOperation )
    {
        this.environment = environment;
        this.oldHostname = oldHostname;
        this.newHostname = newHostname;
        this.trackerOperation = trackerOperation;
    }


    public Environment execute() throws PeerException
    {

        Set<Peer> peers = environment.getPeers();

        for ( final Peer peer : peers )
        {
            peerUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    peer.updateAuthorizedKeysWithNewContainerHostname( environment.getEnvironmentId(), oldHostname,
                            newHostname );

                    return null;
                }
            } ) );
        }

        PeerUtil.PeerTaskResults<Object> peerResults = peerUtil.executeParallel();


        for ( PeerUtil.PeerTaskResult peerResult : peerResults.getPeerTaskResults() )
        {
            if ( peerResult.hasSucceeded() )
            {
                trackerOperation.addLog(
                        String.format( "Updated authorized keys on peer %s", peerResult.getPeer().getName() ) );
            }
            else
            {
                trackerOperation.addLog( String.format( "Failed to update authorized keys on peer %s. Reason: %s",
                        peerResult.getPeer().getName(), peerResult.getFailureReason() ) );
            }
        }


        return environment;
    }
}
