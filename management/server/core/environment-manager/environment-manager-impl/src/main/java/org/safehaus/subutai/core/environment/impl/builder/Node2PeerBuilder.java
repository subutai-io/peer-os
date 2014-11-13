package org.safehaus.subutai.core.environment.impl.builder;


import java.util.List;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.Node2PeerData;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.peer.api.Peer;


/**
 * Created by bahadyr on 11/6/14.
 */
public class Node2PeerBuilder extends EnvironmentBuildProcessFactory
{

    public Node2PeerBuilder( final EnvironmentManagerImpl environmentManager )
    {
        super( environmentManager );
    }


    @Override
    public EnvironmentBuildProcess prepareBuildProcess( final TopologyData topologyData ) throws ProcessBuilderException
    {
        Node2PeerData data = ( Node2PeerData ) topologyData;
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( data.getBlueprintId() );

        for ( Object itemId : data.getMap().keySet() )
        {
            Peer peer = data.getTopology().get( itemId );
            NodeGroup nodeGroup = data.getMap().get( itemId );

            StringBuilder key = new StringBuilder();
            key.append( peer.getId().toString() );
            key.append( nodeGroup.getTemplateName() );

            if ( !process.getMessageMap().containsKey( key.toString() ) )
            {
                CloneContainersMessage ccm = new CloneContainersMessage();
                ccm.setTargetPeerId( peer.getId() );
                ccm.setNodeGroupName( nodeGroup.getName() );
                ccm.setCriteria( null );
                ccm.setNumberOfNodes( 1 );
                ccm.setStrategy( nodeGroup.getPlacementStrategy() );
                List<Template> templates =
                        fetchRequiredTemplates( environmentManager.getPeerManager().getLocalPeer().getId(),
                                nodeGroup.getTemplateName() );

                if ( templates.isEmpty() )
                {
                    throw new ProcessBuilderException( "No templates provided" );
                }


                ccm.setTemplates( templates );
                process.putCloneContainerMessage( key.toString(), ccm );
            }
            else
            {
                process.getMessageMap().get( key.toString() ).incrementNumberOfNodes();
            }
        }



        return process;
    }
}
