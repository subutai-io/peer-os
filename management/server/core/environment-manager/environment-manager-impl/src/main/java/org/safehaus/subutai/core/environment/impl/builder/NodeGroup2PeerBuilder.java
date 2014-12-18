package org.safehaus.subutai.core.environment.impl.builder;


import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.NodeGroup2PeerData;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.peer.api.Peer;


public class NodeGroup2PeerBuilder extends EnvironmentBuildProcessBuilder
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

        for ( Integer itemId : data.getMap().keySet() )
        {
            Peer peer = data.getTopology().get( itemId );
            NodeGroup nodeGroup = data.getMap().get( itemId );

            String key = peer.getId().toString() + "-" + nodeGroup.getTemplateName();

            if ( !process.getMessageMap().containsKey( key ) )
            {
                CloneContainersMessage ccm = makeContainerCloneMessage( nodeGroup, peer.getId() );
                process.putCloneContainerMessage( key, ccm );
            }
        }

        return process;
    }
}

