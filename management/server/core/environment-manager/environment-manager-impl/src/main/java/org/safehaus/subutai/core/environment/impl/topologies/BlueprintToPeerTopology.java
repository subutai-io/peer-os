package org.safehaus.subutai.core.environment.impl.topologies;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.environment.impl.environment.ContainerDistributionMessage;
import org.safehaus.subutai.core.peer.api.Peer;


/**
 * Created by bahadyr on 11/5/14.
 */
public class BlueprintToPeerTopology extends Topology
{


    Peer peer;


    public BlueprintToPeerTopology( final Peer peer )
    {
        this.peer = peer;
    }


    @Override
    public List<ContainerDistributionMessage> digestBlueprint( final EnvironmentBlueprint blueprint )
    {
        List<ContainerDistributionMessage> messages = new ArrayList<>();
        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() )
        {
            ContainerDistributionMessage message = new ContainerDistributionMessage();
            message.setPlacementStrategy( nodeGroup.getPlacementStrategy().toString() );
            message.setNumberOfContainers( nodeGroup.getNumberOfNodes() );
            message.setTargetPeerId( peer.getId() );
            message.setCriterias( null );
            message.setTemplates( fetchRequiredTempaltes() );

            messages.add( message );
        }
        return messages;
    }


    private List<Template> fetchRequiredTempaltes()
    {
        return null;
    }
}
