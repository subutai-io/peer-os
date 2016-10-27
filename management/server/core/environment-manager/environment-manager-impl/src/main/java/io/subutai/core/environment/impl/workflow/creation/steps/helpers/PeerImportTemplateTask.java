package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.exception.ActionFailedException;
import io.subutai.common.peer.Peer;
import io.subutai.common.tracker.TrackerOperation;


public class PeerImportTemplateTask implements Callable<PrepareTemplatesResponse>
{

    private final String environmentId;
    private final Peer peer;
    private final Set<Node> nodes;
    private final TrackerOperation trackerOperation;


    public PeerImportTemplateTask( final String environmentId, final Peer peer, final Set<Node> nodes,
                                   final TrackerOperation trackerOperation )
    {
        this.environmentId = environmentId;
        this.peer = peer;
        this.nodes = nodes;
        this.trackerOperation = trackerOperation;
    }


    @Override
    public PrepareTemplatesResponse call() throws Exception
    {
        Map<String, Set<String>> rhTemplates = new HashMap<>();

        for ( Node node : nodes )
        {
            Set<String> templates = rhTemplates.get( node.getHostId() );

            if ( templates == null )
            {
                templates = Sets.newHashSet();
                rhTemplates.put( node.getHostId(), templates );
            }

            templates.add( node.getTemplateId() );
        }


        PrepareTemplatesResponse response =
                peer.prepareTemplates( new PrepareTemplatesRequest( environmentId, rhTemplates ) );


        for ( String message : response.getMessages() )
        {
            trackerOperation.addLog( message );
        }

        if ( response.hasSucceeded() )
        {
            trackerOperation.addLog( String.format( "Template import succeeded on peer %s", peer.getName() ) );
        }
        else
        {
            trackerOperation.addLog( String.format( "Template import failed on peer %s", peer.getName() ) );

            //throw to let caller fail-fast
            throw new ActionFailedException( String.format( "Template import failed on peer %s", peer.getName() ) );
        }

        return response;
    }
}
