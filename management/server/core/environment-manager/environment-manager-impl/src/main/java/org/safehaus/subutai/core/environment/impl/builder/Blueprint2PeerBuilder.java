package org.safehaus.subutai.core.environment.impl.builder;


import java.util.Set;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.Blueprint2PeerData;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;


public class Blueprint2PeerBuilder extends EnvironmentBuildProcessBuilder
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

            int i = 0;
            for ( NodeGroup nodeGroup : groupSet )
            {
                String key = data.getPeerId().toString() + "-" + nodeGroup.getTemplateName() + "-" + ( i++ );
                CloneContainersMessage ccm = makeContainerCloneMessage( nodeGroup, data.getPeerId() );
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

