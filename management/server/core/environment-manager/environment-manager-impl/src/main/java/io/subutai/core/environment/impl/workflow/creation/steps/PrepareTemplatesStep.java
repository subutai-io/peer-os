package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.workflow.creation.steps.helpers.CreatePeerTemplatePrepareTask;
import io.subutai.core.peer.api.PeerManager;


/**
 * Prepare templates step
 */
public class PrepareTemplatesStep
{
    private static final Logger LOGGER = LoggerFactory.getLogger( PrepareTemplatesStep.class );
    private final Topology topology;
    private PeerManager peerManager;
    private TrackerOperation operationTracker;


    public PrepareTemplatesStep( final PeerManager peerManager, final Topology topology,
                                 final TrackerOperation operationTracker )
    {
        this.topology = topology;
        this.peerManager = peerManager;
        this.operationTracker = operationTracker;
    }


    public void execute() throws EnvironmentCreationException, PeerException
    {
        Map<String, Set<Node>> placement = topology.getNodeGroupPlacement();

        PeerUtil<PrepareTemplatesResponse> templateUtil = new PeerUtil<>();

        for ( Map.Entry<String, Set<Node>> peerPlacement : placement.entrySet() )
        {
            Peer peer = peerManager.getPeer( peerPlacement.getKey() );

            templateUtil.addPeerTask( new PeerUtil.PeerTask<>( peer,
                    new CreatePeerTemplatePrepareTask( peer, peerPlacement.getValue() ) ) );
        }

        PeerUtil.PeerTaskResults<PrepareTemplatesResponse> templateResults = templateUtil.executeParallel();

        //collect results
        boolean succeeded = true;

        for ( PeerUtil.PeerTaskResult<PrepareTemplatesResponse> templateResult : templateResults.getPeerTaskResults() )
        {
            PrepareTemplatesResponse prepareTemplatesResponse = templateResult.getResult();

            succeeded &= prepareTemplatesResponse.hasSucceeded();

            for ( String message : prepareTemplatesResponse.getMessages() )
            {
                operationTracker.addLog( message );
            }
        }

        if ( !succeeded )
        {
            throw new EnvironmentCreationException( "There were errors during preparation templates." );
        }
    }
}
