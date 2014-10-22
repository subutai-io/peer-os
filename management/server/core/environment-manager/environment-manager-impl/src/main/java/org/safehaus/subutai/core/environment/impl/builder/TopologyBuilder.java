package org.safehaus.subutai.core.environment.impl.builder;


import java.util.Map;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.peer.api.Peer;


/**
 * Created by bahadyr on 10/21/14.
 */
public class TopologyBuilder
{


    public EnvironmentBuildProcess createEnvironmentBuildProcessN2P( String blueprintName, Map<Object, Peer> topology,
                                                                     Map<Object, NodeGroup> map )
    {
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( blueprintName );

        for ( Object itemId : map.keySet() )
        {
            Peer peer = topology.get( itemId );
            NodeGroup ng = map.get( itemId );

            StringBuilder key = new StringBuilder();
            key.append( peer.getId().toString() );
            key.append( ng.getTemplateName() );

            if ( !process.getMessageMap().containsKey( key.toString() ) )
            {
                CloneContainersMessage ccm = new CloneContainersMessage( process.getUuid(), peer.getId() );
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


    public EnvironmentBuildProcess createEnvironmentBuildProcessNG2Peer( String blueprintName,
                                                                         Map<Object, Peer> topology,
                                                                         Map<Object, NodeGroup> map )
    {
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( blueprintName );

        for ( Object itemId : map.keySet() )
        {
            Peer peer = topology.get( itemId );
            NodeGroup ng = map.get( itemId );

            String key = peer.getId().toString() + "-" + ng.getTemplateName();

            if ( !process.getMessageMap().containsKey( key ) )
            {
                CloneContainersMessage ccm = new CloneContainersMessage( process.getUuid(), peer.getId() );
                ccm.setTemplate( ng.getTemplateName() );
                ccm.setNumberOfNodes( ng.getNumberOfNodes() );
                ccm.setStrategy( ng.getPlacementStrategy().toString() );
                process.putCloneContainerMessage( key, ccm );
            }
        }

        return process;
    }
}
