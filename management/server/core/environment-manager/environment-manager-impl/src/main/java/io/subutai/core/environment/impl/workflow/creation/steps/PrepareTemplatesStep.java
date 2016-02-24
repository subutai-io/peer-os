package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
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
        Map<String, Set<NodeGroup>> placement = topology.getNodeGroupPlacement();

        ExecutorService taskExecutor = Executors.newFixedThreadPool( placement.size() );

        CompletionService<PrepareTemplatesResponse> taskCompletionService = getCompletionService( taskExecutor );


        for ( Map.Entry<String, Set<NodeGroup>> peerPlacement : placement.entrySet() )
        {
            Peer peer = peerManager.getPeer( peerPlacement.getKey() );
            LOGGER.debug( String.format( "Scheduling template preparation task on peer %s", peer.getId() ) );

            taskCompletionService.submit( new CreatePeerTemplatePrepareTask( peer, peerPlacement.getValue() ) );
        }

        taskExecutor.shutdown();

        //collect results
        Set<String> errors = Sets.newHashSet();

        for ( int i = 0; i < placement.size(); i++ )
        {
            try
            {
                Future<PrepareTemplatesResponse> futures = taskCompletionService.take();
                final PrepareTemplatesResponse prepareTemplatesResponse = futures.get();
                if ( prepareTemplatesResponse.getDescription() != null )
                {
                    operationTracker.addLog( prepareTemplatesResponse.getDescription() );
                }
                if ( !prepareTemplatesResponse.getResult() )
                {
                    throw new EnvironmentCreationException( String.format(
                            "There were errors during preparation of templates. " + prepareTemplatesResponse
                                    .getDescription() ) );
                }
            }
            catch ( ExecutionException | InterruptedException e )
            {
                throw new EnvironmentCreationException(
                        String.format( "There were errors during preparation templates:  %s", errors ) );
            }
        }
    }


    protected CompletionService<PrepareTemplatesResponse> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }
}
