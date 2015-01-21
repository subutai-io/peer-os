package org.safehaus.subutai.core.env.impl.builder;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.env.api.build.NodeGroup;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import org.safehaus.subutai.core.env.impl.exception.NodeGroupBuildException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Creates node groups on a peer
 */
public class NodeGroupBuilder
{

    private final TemplateRegistry templateRegistry;
    private final PeerManager peerManager;


    public NodeGroupBuilder( final TemplateRegistry templateRegistry, final PeerManager peerManager )
    {
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
    }


    public Set<EnvironmentContainerImpl> build( Peer peer, Set<NodeGroup> nodeGroups ) throws NodeGroupBuildException
    {
        Preconditions.checkNotNull( peer );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( nodeGroups ) );

        Set<EnvironmentContainerImpl> containers = Sets.newHashSet();

        UUID localPeerId = peerManager.getLocalPeer().getId();

        for ( NodeGroup nodeGroup : nodeGroups )
        {
            try
            {
                //TODO revise peer container cloning:
                // 1) add batch set<nodeGroup> cloning
                // 2) replace creatorPeerId with ownerId or remove at all
                // 3) review template sharing and parameter passing


                Set<HostInfoModel> newHosts = peer.scheduleCloneContainers( localPeerId,
                        fetchRequiredTemplates( peer.getId(), nodeGroup.getTemplateName() ),
                        nodeGroup.getNumberOfNodes(), nodeGroup.getNodePlacementStrategy().getStrategyId(),
                        nodeGroup.getNodePlacementStrategy().getCriteriaAsList() );


                for ( HostInfoModel newHost : newHosts )
                {
                    containers.add( new EnvironmentContainerImpl( peer, nodeGroup.getName(), newHost,
                            templateRegistry.getTemplate( nodeGroup.getTemplateName() ) ) );
                }
            }
            catch ( PeerException e )
            {
                throw new NodeGroupBuildException(
                        String.format( "Error creating node group %s on peer %s", nodeGroup, peer ), e );
            }
        }

        return containers;
    }


    public List<Template> fetchRequiredTemplates( UUID sourcePeerId, final String templateName )
            throws NodeGroupBuildException
    {
        List<Template> requiredTemplates = Lists.newArrayList();
        List<Template> templates = templateRegistry.getParentTemplates( templateName );

        Template installationTemplate = templateRegistry.getTemplate( templateName );
        if ( installationTemplate != null )
        {
            templates.add( installationTemplate );
        }
        else
        {
            throw new NodeGroupBuildException( String.format( "Template %s is not found in registry", templateName ),
                    null );
        }


        for ( Template t : templates )
        {
            requiredTemplates.add( t.getRemoteClone( sourcePeerId ) );
        }

        if ( requiredTemplates.isEmpty() )
        {
            throw new NodeGroupBuildException( "Could not fetch template information", null );
        }

        return requiredTemplates;
    }
}
