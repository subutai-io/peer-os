package org.safehaus.subutai.core.environment.impl.builder;


import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.NodeGroup2PeerGroupData;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerGroup;


public class NodeGroup2PeerGroupBuilder extends EnvironmentBuildProcessBuilder
{

    public NodeGroup2PeerGroupBuilder( final EnvironmentManagerImpl environmentManager )
    {
        super( environmentManager );
    }


    @Override
    public EnvironmentBuildProcess prepareBuildProcess( final TopologyData topologyData ) throws ProcessBuilderException
    {
        if ( !( topologyData instanceof NodeGroup2PeerGroupData ) )
        {
            throw new ProcessBuilderException( "Invalid topology data type" );
        }

        NodeGroup2PeerGroupData data = ( NodeGroup2PeerGroupData ) topologyData;
        PeerGroup peerGroup = environmentManager.getPeerManager().getPeerGroup( data.getPeerGroupId() );

        EnvironmentBlueprint blueprint;
        try
        {
            blueprint = environmentManager.getEnvironmentBlueprint( data.getBlueprintId() );
        }
        catch ( EnvironmentManagerException e )
        {
            throw new ProcessBuilderException( e.getMessage() );
        }

        EnvironmentBuildProcess process = new EnvironmentBuildProcess( data.getBlueprintId() );
        for ( Map.Entry<NodeGroup, UUID> e : data.getNodeGroupToPeer().entrySet() )
        {
            NodeGroup ng = e.getKey();
            UUID peerId = e.getValue();
            if ( !blueprint.getNodeGroups().contains( ng ) )
            {
                throw new ProcessBuilderException( "Node group not found in blueprint" );
            }
            if ( !peerGroup.getPeerIds().contains( peerId ) )
            {
                String m = String.format( "Peer id=%s does not belong to peer group %s", peerId, peerGroup.getName() );
                throw new ProcessBuilderException( m );
            }

            Peer peer = environmentManager.getPeerManager().getPeer( peerId );
            if ( peer == null )
            {
                throw new ProcessBuilderException( "Peer not found: id=" + peerId );
            }

            CloneContainersMessage ccm = makeContainerCloneMessage( ng, peerId );

            String key = peer.getId() + ng.getName();
            process.putCloneContainerMessage( key, ccm );
        }
        return process;
    }

}

