package org.safehaus.subutai.core.environment.impl.builder;


import java.util.List;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.NodeGroup2PeerData;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.peer.api.Peer;


/**
 * Created by bahadyr on 11/6/14.
 */
public class NodeGroup2PeerBuilder extends EnvironmentBuildProcessFactory
{
    public NodeGroup2PeerBuilder( final EnvironmentManagerImpl environmentManager )
    {
        super( environmentManager );
    }


    @Override
    public EnvironmentBuildProcess prepareBuildProcess( final TopologyData topologyData ) throws ProcessBuilderException
    {
        NodeGroup2PeerData data = ( NodeGroup2PeerData ) topologyData;
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( data.getBlueprintId() );

        for ( Object itemId : data.getMap().keySet() )
        {
            Peer peer = data.getTopology().get( itemId );
            NodeGroup nodeGroup = data.getMap().get( itemId );

            String key = peer.getId().toString() + "-" + nodeGroup.getTemplateName();

            if ( !process.getMessageMap().containsKey( key ) )
            {
                CloneContainersMessage ccm = new CloneContainersMessage();
                ccm.setTargetPeerId( peer.getId() );
                ccm.setNodeGroupName( nodeGroup.getName() );
                ccm.setNumberOfNodes( nodeGroup.getNumberOfNodes() );
                ccm.setStrategy( nodeGroup.getPlacementStrategy() );
                List<Template> templates =
                        fetchRequiredTemplates( environmentManager.getPeerManager().getLocalPeer().getId(),
                                nodeGroup.getTemplateName() );

                if ( templates.isEmpty() )
                {
                    throw new ProcessBuilderException( "No templates provided" );
                }

                ccm.setTemplates( templates );
                process.putCloneContainerMessage( key, ccm );
            }
        }

        return process;
    }
}
