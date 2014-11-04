package org.safehaus.subutai.core.environment.impl.builder;


import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerGroup;

import com.google.common.collect.Lists;


/**
 * Created by bahadyr on 10/21/14.
 */
public class TopologyBuilder
{

    EnvironmentManagerImpl environmentManager;


    public TopologyBuilder( final EnvironmentManagerImpl environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public EnvironmentBuildProcess createEnvironmentBuildProcessN2P( UUID blueprintId, Map<Object, Peer> topology,
                                                                     Map<Object, NodeGroup> map )
    {
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( blueprintId );

        for ( Object itemId : map.keySet() )
        {
            Peer peer = topology.get( itemId );
            NodeGroup ng = map.get( itemId );

            StringBuilder key = new StringBuilder();
            key.append( peer.getId().toString() );
            key.append( ng.getTemplateName() );

            if ( !process.getMessageMap().containsKey( key.toString() ) )
            {
                CloneContainersMessage ccm = new CloneContainersMessage( peer.getId() );
                ccm.setEnvId( process.getId() );
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


    public EnvironmentBuildProcess createEnvironmentBuildProcessNG2Peer( UUID blueprintId, Map<Object, Peer> topology,
                                                                         Map<Object, NodeGroup> map )
    {
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( blueprintId );

        for ( Object itemId : map.keySet() )
        {
            Peer peer = topology.get( itemId );
            NodeGroup ng = map.get( itemId );

            String key = peer.getId().toString() + "-" + ng.getTemplateName();

            if ( !process.getMessageMap().containsKey( key ) )
            {
                CloneContainersMessage ccm = new CloneContainersMessage( peer.getId() );
                ccm.setEnvId( process.getId() );
                ccm.setTemplate( ng.getTemplateName() );
                ccm.setNumberOfNodes( ng.getNumberOfNodes() );
                ccm.setStrategy( ng.getPlacementStrategy().toString() );
                process.putCloneContainerMessage( key, ccm );
            }
        }

        return process;
    }


    public EnvironmentBuildProcess createEnvironmentBuildProcessB2PG( final UUID blueprintId, final UUID peerGroupId )
            throws EnvironmentBuildException
    {
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( blueprintId );
        try
        {
            EnvironmentBlueprint blueprint = environmentManager.getEnvironmentBlueprint( blueprintId );
            PeerGroup peerGroup = environmentManager.getPeerManager().getPeerGroup( peerGroupId );

            List<UUID> uuidList = Lists.newArrayList( peerGroup.getPeerIds().iterator() );

            Set<NodeGroup> groupSet = blueprint.getNodeGroups();
            for ( NodeGroup nodeGroup : groupSet )
            {
                UUID peerId = uuidList.get( randomInt( uuidList.size() ) );
                String key = peerId.toString() + "-" + nodeGroup.getTemplateName();
                CloneContainersMessage ccm = new CloneContainersMessage( peerId );
                ccm.setEnvId( process.getId() );
                ccm.setTemplate( nodeGroup.getTemplateName() );
                ccm.setNumberOfNodes( nodeGroup.getNumberOfNodes() );
                ccm.setStrategy( nodeGroup.getPlacementStrategy().toString() );
                process.putCloneContainerMessage( key, ccm );
            }

            return process;
        }
        catch ( EnvironmentManagerException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }
    }


    public EnvironmentBuildProcess createEnvironmentBuildProcessNG2PG( final UUID blueprintId, final UUID peerGroupId )
            throws EnvironmentBuildException
    {
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( blueprintId );
        try
        {
            EnvironmentBlueprint blueprint = environmentManager.getEnvironmentBlueprint( blueprintId );
            PeerGroup peerGroup = environmentManager.getPeerManager().getPeerGroup( peerGroupId );

            List<UUID> uuidList = Lists.newArrayList( peerGroup.getPeerIds().iterator() );

            Set<NodeGroup> groupSet = blueprint.getNodeGroups();
            for ( NodeGroup nodeGroup : groupSet )
            {
                UUID peerId = uuidList.get( randomInt( uuidList.size() ) );
                String key = peerId.toString() + "-" + nodeGroup.getTemplateName();
                CloneContainersMessage ccm = new CloneContainersMessage( peerId );
                ccm.setEnvId( process.getId() );
                ccm.setTemplate( nodeGroup.getTemplateName() );
                ccm.setNumberOfNodes( nodeGroup.getNumberOfNodes() );
                ccm.setStrategy( nodeGroup.getPlacementStrategy().toString() );
                process.putCloneContainerMessage( key, ccm );
            }

            return process;
        }
        catch ( EnvironmentManagerException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }
    }


    private int randomInt( int max )
    {
        Random random = new Random();
        int rand = random.nextInt( max + 1 );
        return rand;
    }


    public EnvironmentBuildProcess createEnvironmentBuildProcessB2P( final UUID blueprintId, final UUID peerId )
            throws EnvironmentBuildException
    {
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( blueprintId );
        try
        {
            EnvironmentBlueprint blueprint = environmentManager.getEnvironmentBlueprint( blueprintId );
            Set<NodeGroup> groupSet = blueprint.getNodeGroups();
            for ( NodeGroup nodeGroup : groupSet )
            {
                String key = peerId.toString() + "-" + nodeGroup.getTemplateName();
                CloneContainersMessage ccm = new CloneContainersMessage( peerId );
                ccm.setEnvId( process.getId() );
                ccm.setTemplate( nodeGroup.getTemplateName() );
                ccm.setNumberOfNodes( nodeGroup.getNumberOfNodes() );
                ccm.setStrategy( nodeGroup.getPlacementStrategy().toString() );
                process.putCloneContainerMessage( key, ccm );
            }

            return process;
        }
        catch ( EnvironmentManagerException e )
        {
            throw new EnvironmentBuildException( e.getMessage() );
        }
    }
}
