package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.exception.NodeGroupBuildException;
import io.subutai.core.kurjun.api.TemplateManager;


public class CreatePeerTemplatePrepareTask implements Callable<PrepareTemplatesResponse>
{
    private static final Logger LOG = LoggerFactory.getLogger( CreatePeerTemplatePrepareTask.class );

    private final Peer peer;
    private final Set<NodeGroup> nodeGroups;


    public CreatePeerTemplatePrepareTask( final Peer peer, final Set<NodeGroup> nodeGroups )
    {
        this.peer = peer;
        this.nodeGroups = nodeGroups;
    }


    @Override
    public PrepareTemplatesResponse call() throws Exception
    {
        Map<String, Set<String>> rhTemplates = new HashMap<>();
        for ( NodeGroup nodeGroup : nodeGroups )
        {
            Set<String> templates = rhTemplates.get( nodeGroup.getHostId() );
            if ( templates == null )
            {
                templates = new HashSet<>();
                rhTemplates.put( nodeGroup.getHostId(), templates );
            }
            templates.add( nodeGroup.getTemplateName() );
        }

        return peer.prepareTemplates(new PrepareTemplatesRequest( rhTemplates ) );
    }
}
