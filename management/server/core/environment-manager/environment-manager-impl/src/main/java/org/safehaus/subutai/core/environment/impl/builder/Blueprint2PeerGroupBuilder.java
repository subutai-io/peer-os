package org.safehaus.subutai.core.environment.impl.builder;


import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.Blueprint2PeerGroupData;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.peer.api.PeerGroup;

import com.google.common.collect.Lists;


/**
 * Created by bahadyr on 11/6/14.
 */
public class Blueprint2PeerGroupBuilder extends EnvironmentBuildProcessFactory
{

    public Blueprint2PeerGroupBuilder( final EnvironmentManagerImpl environmentManager )
    {
        super( environmentManager );
    }


    @Override
    public EnvironmentBuildProcess prepareBuildProcess( final TopologyData topologyData ) throws ProcessBuilderException
    {
        Blueprint2PeerGroupData data = ( Blueprint2PeerGroupData ) topologyData;
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( data.getBlueprintId() );
        try
        {
            EnvironmentBlueprint blueprint = environmentManager.getEnvironmentBlueprint( data.getBlueprintId() );
            PeerGroup peerGroup = environmentManager.getPeerManager().getPeerGroup( data.getPeerGroupId() );

            List<UUID> uuidList = Lists.newArrayList( peerGroup.getPeerIds().iterator() );

            Set<NodeGroup> groupSet = blueprint.getNodeGroups();
            for ( NodeGroup nodeGroup : groupSet )
            {
                UUID peerId = uuidList.get( randomInt( uuidList.size() ) );
                String key = peerId.toString() + "-" + nodeGroup.getTemplateName();
                CloneContainersMessage ccm = new CloneContainersMessage();
                ccm.setTargetPeerId( peerId );
                ccm.setNodeGroupName( nodeGroup.getName() );
                ccm.setNumberOfNodes( nodeGroup.getNumberOfNodes() );
                ccm.setStrategy( nodeGroup.getPlacementStrategy() );
                List<Template> templates =
                        fetchRequiredTemplates( environmentManager.getPeerManager().getLocalPeer().getId(),
                                nodeGroup.getTemplateName() );
                ccm.setTemplates( templates );
                process.putCloneContainerMessage( key, ccm );
            }

            return process;
        }
        catch ( EnvironmentManagerException e )
        {
            throw new ProcessBuilderException( e.getMessage() );
        }
    }


    /**
     * TODO: should be replaced with placement strategy
     */
    private int randomInt( int max )
    {
        Random random = new Random();
        int rand = random.nextInt( max + 1 );
        return rand;
    }
}
