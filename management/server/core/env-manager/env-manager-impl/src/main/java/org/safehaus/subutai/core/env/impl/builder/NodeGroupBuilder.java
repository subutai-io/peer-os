package org.safehaus.subutai.core.env.impl.builder;


import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.env.api.build.NodeGroup;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import org.safehaus.subutai.core.env.impl.exception.NodeGroupBuildException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Creates node groups on a peer
 */
public class NodeGroupBuilder implements Callable<Set<EnvironmentContainerImpl>>
{

    private final TemplateRegistry templateRegistry;
    private final PeerManager peerManager;
    private final Peer peer;
    private final Set<NodeGroup> nodeGroups;


    public NodeGroupBuilder( final TemplateRegistry templateRegistry, final PeerManager peerManager, final Peer peer,
                             final Set<NodeGroup> nodeGroups )
    {
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( peer );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( nodeGroups ) );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.peer = peer;
        this.nodeGroups = nodeGroups;
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


    @Override
    public Set<EnvironmentContainerImpl> call() throws NodeGroupBuildException
    {

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
                        nodeGroup.getNumberOfContainers(), nodeGroup.getContainerPlacementStrategy().getStrategyId(),
                        nodeGroup.getContainerPlacementStrategy().getCriteriaAsList() );


                for ( HostInfoModel newHost : newHosts )
                {
                    containers.add( new EnvironmentContainerImpl( peer, nodeGroup.getName(), newHost,
                            templateRegistry.getTemplate( nodeGroup.getTemplateName() ), nodeGroup.getSshGroupId(),
                            nodeGroup.getHostsGroupId(), nodeGroup.getDomainName() ) );
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
}
