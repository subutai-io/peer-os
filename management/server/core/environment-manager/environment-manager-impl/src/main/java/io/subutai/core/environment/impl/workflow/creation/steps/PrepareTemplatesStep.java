package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.workflow.creation.steps.helpers.PeerImportTemplateTask;
import io.subutai.core.peer.api.PeerManager;


/**
 * Prepare templates step
 */
public class PrepareTemplatesStep
{
    private static final Logger LOGGER = LoggerFactory.getLogger( PrepareTemplatesStep.class );

    private final Environment environment;
    private final Topology topology;
    private final PeerManager peerManager;
    private final TrackerOperation operationTracker;


    public PrepareTemplatesStep( final Environment environment, final PeerManager peerManager, final Topology topology,
                                 final TrackerOperation operationTracker )
    {
        this.environment = environment;
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
                    new PeerImportTemplateTask( environment.getId(), peer, peerPlacement.getValue(),
                            operationTracker ) ) );
        }

        PeerUtil.PeerTaskResults<PrepareTemplatesResponse> templateResults = templateUtil.executeParallelFailFast();

        boolean succeeded = true;

        for ( PeerUtil.PeerTaskResult<PrepareTemplatesResponse> templateResult : templateResults.getPeerTaskResults() )
        {
            succeeded &= templateResult.getResult().hasSucceeded();
        }

        if ( !succeeded )
        {
            throw new EnvironmentCreationException( "Failed to import templates on all peers" );
        }
    }
}
