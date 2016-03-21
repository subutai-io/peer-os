package io.subutai.core.hubmanager.impl.environment;


import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesResponseCollector;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.task.ImportTemplateResponse;
import io.subutai.common.tracker.OperationMessage;


class TemplateHelper extends Helper
{
    TemplateHelper( LocalPeer localPeer )
    {
        super( localPeer );
    }


    @Override
    void execute( PeerEnvironmentDto dto ) throws InterruptedException, ExecutionException
    {
        Node node = new Node( dto.getContainerHostname(), dto.getContainerName(), dto.getTemplateName(), dto.getContainerSize(), 0, 0,
                localPeer.getId(), getFirstResourceHostId( localPeer ) );

        ExecutorService exec = Executors.newSingleThreadScheduledExecutor();

        CompletionService<PrepareTemplatesResponseCollector> taskCompletionService = getCompletionService( exec );

        taskCompletionService.submit( new CreatePeerTemplatePrepareTask( localPeer, Sets.newHashSet( node ) ) );

        exec.shutdown();

        Future<PrepareTemplatesResponseCollector> future = taskCompletionService.take();

        PrepareTemplatesResponseCollector response = future.get();

        log.debug( "Response count: {}", response.getResponses().size() );

        for ( ImportTemplateResponse importTemplateResponse : response.getResponses() )
        {
            log.debug( "{}", importTemplateResponse );
        }

        for ( OperationMessage msg : response.getOperationMessages() )
        {
            log.debug( "{}", msg );
        }
    }


    private CompletionService<PrepareTemplatesResponseCollector> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }
}
