package org.safehaus.subutai.core.environment.impl.builder;


import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.Node2PeerData;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.peer.api.Peer;


/**
 * Created by bahadyr on 11/6/14.
 */
public class Node2PeerBuilder extends TopologyBuilder
{

    public Node2PeerBuilder( final EnvironmentManagerImpl environmentManager )
    {
        super( environmentManager );
    }


    @Override
    public EnvironmentBuildProcess prepareBuildProcess( final TopologyData topologyData )
    {
        Node2PeerData data = ( Node2PeerData ) topologyData;
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( data.getBlueprintId() );

        for ( Object itemId : data.getMap().keySet() )
        {
            Peer peer = data.getTopology().get( itemId );
            NodeGroup ng = data.getMap().get( itemId );

            StringBuilder key = new StringBuilder();
            key.append( peer.getId().toString() );
            key.append( ng.getTemplateName() );

            if ( !process.getMessageMap().containsKey( key.toString() ) )
            {
                CloneContainersMessage ccm = new CloneContainersMessage( peer.getId() );
                ccm.setEnvId( process.getId() );
                ccm.setNodeGroupName( ng.getName() );
                ccm.setTemplate( ng.getTemplateName() );
                ccm.setNumberOfNodes( 1 );
                ccm.setStrategy( ng.getPlacementStrategy().toString() );
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
