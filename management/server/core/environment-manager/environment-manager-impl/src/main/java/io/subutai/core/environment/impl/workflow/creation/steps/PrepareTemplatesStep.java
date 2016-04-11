package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.StringUtils;

import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesResponseCollector;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.task.ImportTemplateResponse;
import io.subutai.common.tracker.OperationMessage;
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

        PeerUtil<PrepareTemplatesResponseCollector> templateUtil = new PeerUtil<>();

        for ( Map.Entry<String, Set<Node>> peerPlacement : placement.entrySet() )
        {
            Peer peer = peerManager.getPeer( peerPlacement.getKey() );

            templateUtil.addPeerTask( new PeerUtil.PeerTask<>( peer,
                    new CreatePeerTemplatePrepareTask( peer, peerPlacement.getValue() ) ) );
        }

        PeerUtil.PeerTaskResults<PrepareTemplatesResponseCollector> templateResults = templateUtil.executeParallel();

        //collect results
        boolean succeeded = true;

        for ( PeerUtil.PeerTaskResult<PrepareTemplatesResponseCollector> templateResult : templateResults
                .getPeerTaskResults() )
        {
            PrepareTemplatesResponseCollector prepareTemplatesResponse = templateResult.getResult();
            succeeded &= prepareTemplatesResponse.hasSucceeded();
            addLogs( prepareTemplatesResponse );
            processResponse( prepareTemplatesResponse );
        }

        if ( !succeeded )
        {
            throw new EnvironmentCreationException( "There were errors during preparation templates." );
        }
    }


    private void processResponse( final PrepareTemplatesResponseCollector response )
    {
        for ( ImportTemplateResponse importTemplateResponse : response.getResponses() )
        {
            LOGGER.debug( String.format( "Import response: %s", importTemplateResponse ) );
        }
    }


    private void addLogs( final PrepareTemplatesResponseCollector result )
    {
        for ( OperationMessage message : result.getOperationMessages() )
        {
            operationTracker.addLog( message.getValue() );
            if ( !result.hasSucceeded() && StringUtils.isNotBlank( message.getDescription() ) )
            {
                LOGGER.error( message.getDescription() );
            }
        }
    }
}
