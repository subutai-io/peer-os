package org.safehaus.subutai.core.environment.impl.builder;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.Blueprint2PeerData;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;


/**
 * Created by bahadyr on 11/6/14.
 */
public class Blueprint2PeerBuilder extends EnvironmentBuildProcessFactory
{
    public Blueprint2PeerBuilder( final EnvironmentManagerImpl environmentManager )
    {
        super( environmentManager );
    }


    @Override
    public EnvironmentBuildProcess prepareBuildProcess( final TopologyData topologyData )
            throws ProcessBuilderException
    {
        Blueprint2PeerData data = ( Blueprint2PeerData ) topologyData;
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( data.getBlueprintId() );
        try
        {
            EnvironmentBlueprint blueprint = environmentManager.getEnvironmentBlueprint( data.getBlueprintId() );
            Set<NodeGroup> groupSet = blueprint.getNodeGroups();

            int i=0;
            for ( NodeGroup nodeGroup : groupSet )
            {
                String key = data.getPeerId().toString() + "-" + nodeGroup.getTemplateName()+"-"+(i++);
                CloneContainersMessage ccm = new CloneContainersMessage();
                ccm.setTargetPeerId( data.getPeerId() );
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

            return process;
        }
        catch ( EnvironmentManagerException e )
        {
            throw new ProcessBuilderException( e.getMessage() );
        }
    }
}
