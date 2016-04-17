package io.subutai.core.hubmanager.impl;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.peer.Peer;


public class CreatePeerTemplatePrepareTask implements Callable<PrepareTemplatesResponse>
{
    private static final Logger LOG = LoggerFactory.getLogger( CreatePeerTemplatePrepareTask.class );

    private final String environmentId;
    private final Peer peer;
    private final Set<Node> nodes;


    public CreatePeerTemplatePrepareTask( final String environmentId, final Peer peer, final Set<Node> nodes )
    {
        this.environmentId = environmentId;
        this.peer = peer;
        this.nodes = nodes;
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
                templates = new HashSet<>();
                rhTemplates.put( node.getHostId(), templates );
            }
            templates.add( node.getTemplateName() );
        }

        return peer.prepareTemplates( new PrepareTemplatesRequest( environmentId, rhTemplates ) );
    }
}
