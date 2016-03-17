package io.subutai.core.hubmanager.impl.environment;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.environment.PrepareTemplatesResponseCollector;
import io.subutai.common.peer.Peer;


public class CreatePeerTemplatePrepareTask implements Callable<PrepareTemplatesResponseCollector>
{
    private static final Logger LOG = LoggerFactory.getLogger( CreatePeerTemplatePrepareTask.class );

    private final Peer peer;
    private final Set<Node> nodes;


    public CreatePeerTemplatePrepareTask( final Peer peer, final Set<Node> nodes )
    {
        this.peer = peer;
        this.nodes = nodes;
    }


    @Override
    public PrepareTemplatesResponseCollector call() throws Exception
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

        return peer.prepareTemplates(new PrepareTemplatesRequest( rhTemplates ) );
    }
}
