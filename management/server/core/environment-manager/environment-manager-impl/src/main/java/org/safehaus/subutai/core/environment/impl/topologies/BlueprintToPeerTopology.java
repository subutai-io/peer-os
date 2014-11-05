package org.safehaus.subutai.core.environment.impl.topologies;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.environment.impl.environment.ContainerDistributionMessage;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;


/**
 * Created by bahadyr on 11/5/14.
 */
public class BlueprintToPeerTopology extends Topology
{

    private final Peer peer;


    public BlueprintToPeerTopology( final Peer peer, TemplateRegistry templateRegistry )
    {
        super( templateRegistry );
        this.peer = peer;
    }


    /**
     * Prepares list of messages from Blueprint to build environment.
     */
    @Override
    public List<ContainerDistributionMessage> digestBlueprint( final EnvironmentBlueprint blueprint,
                                                               UUID environmentId )
    {
        List<ContainerDistributionMessage> messages = new ArrayList<>();
        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() )
        {
            ContainerDistributionMessage message = new ContainerDistributionMessage();
            message.setPlacementStrategy( nodeGroup.getPlacementStrategy().toString() );
            message.setNumberOfContainers( nodeGroup.getNumberOfNodes() );
            message.setTargetPeerId( peer.getId() );
            message.setCriterias( null );
            message.setEnvironmentId( environmentId );
            message.setTemplates( fetchRequiredTempaltes( peer.getId(), nodeGroup.getTemplateName() ) );
            messages.add( message );
        }
        return messages;
    }


    /**
     * Fetches the template information required to build environment
     */
    private List<Template> fetchRequiredTempaltes( UUID sourcePeerId, final String templateName )
    {
        List<Template> requiredTemplates = new ArrayList<>();
        List<Template> templates = templateRegistry.getParentTemplates( templateName );

        Template installationTemplate = templateRegistry.getTemplate( templateName );
        if ( installationTemplate != null )
        {
            templates.add( installationTemplate );
        }


        for ( Template t : templates )
        {
            requiredTemplates.add( t.getRemoteClone( sourcePeerId ) );
        }

        return requiredTemplates;
    }
}
