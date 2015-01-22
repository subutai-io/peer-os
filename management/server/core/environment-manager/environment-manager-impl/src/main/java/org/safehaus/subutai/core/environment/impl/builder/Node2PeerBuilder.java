package org.safehaus.subutai.core.environment.impl.builder;


import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.Node2PeerData;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;


public class Node2PeerBuilder extends EnvironmentBuildProcessBuilder
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

        for ( Integer itemId : data.getMap().keySet() )
        {
            Peer peer = data.getTopology().get( itemId );
            NodeGroup nodeGroup = data.getMap().get( itemId );

            StringBuilder key = new StringBuilder();
            key.append( peer.getId().toString() );
            key.append( nodeGroup.getTemplateName() );

            if ( !process.getMessageMap().containsKey( key.toString() ) )
            {
                CloneContainersMessage ccm = makeContainerCloneMessage( nodeGroup, peer.getId() );
                // reset number of nodes to one!!!
                ccm.setNumberOfNodes( 1 );

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

