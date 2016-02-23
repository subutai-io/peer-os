package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.exception.NodeGroupBuildException;
import io.subutai.core.kurjun.api.TemplateManager;


public class CreatePeerNodeGroupsTask implements Callable<Set<NodeGroupBuildResult>>
{
    private static final Logger LOG = LoggerFactory.getLogger( CreatePeerNodeGroupsTask.class );

    private final Peer peer;
    private final Set<NodeGroup> nodeGroups;
    private final LocalPeer localPeer;
    private final Environment environment;
    private final int ipAddressOffset;
    private final TemplateManager templateRegistry;
    private final String defaultDomain;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();


    public CreatePeerNodeGroupsTask( final Peer peer, final Set<NodeGroup> nodeGroups, final LocalPeer localPeer,
                                     final Environment environment, final int ipAddressOffset,
                                     final TemplateManager templateRegistry, final String defaultDomain )
    {
        this.peer = peer;
        this.nodeGroups = nodeGroups;
        this.localPeer = localPeer;
        this.environment = environment;
        this.ipAddressOffset = ipAddressOffset;
        this.templateRegistry = templateRegistry;
        this.defaultDomain = defaultDomain;
    }


    @Override
    public Set<NodeGroupBuildResult> call() throws Exception
    {
        final Set<NodeGroupBuildResult> results = Sets.newHashSet();
        int currentIpAddressOffset = 0;

        for ( NodeGroup nodeGroup : nodeGroups )
        {
            LOG.debug( String.format( "Scheduling on %s %s", nodeGroup.getPeerId(), nodeGroup.getName() ) );
            ContainerSize containerSize = nodeGroup.getType();
            NodeGroupBuildException exception = null;
            Set<EnvironmentContainerImpl> containers = Sets.newHashSet();
            try
            {
                final CreateEnvironmentContainerGroupRequest request;
                final String hostname =
                        String.format( "%s_%s", nodeGroup.getTemplateName(), UUID.randomUUID().toString() );
                request = new CreateEnvironmentContainerGroupRequest( hostname, environment.getId(), localPeer.getId(),
                        localPeer.getOwnerId(), environment.getSubnetCidr(), ipAddressOffset + currentIpAddressOffset,
                        nodeGroup.getTemplateName(), nodeGroup.getHostId(), nodeGroup.getType() );

                Set<ContainerHostInfoModel> newHosts = peer.createEnvironmentContainerGroup( request );

                currentIpAddressOffset++;

                for ( ContainerHostInfoModel newHost : newHosts )
                {

                    containers.add( new EnvironmentContainerImpl( localPeer.getId(), peer, nodeGroup.getName(), newHost,
                            templateRegistry.getTemplate( nodeGroup.getTemplateName() ), nodeGroup.getSshGroupId(),
                            nodeGroup.getHostsGroupId(), defaultDomain, containerSize ) );
                }

                if ( containers.isEmpty() )
                {
                    exception = new NodeGroupBuildException( "Requested container has not been created", null );
                }
            }
            catch ( Exception e )
            {
                exception = new NodeGroupBuildException(
                        String.format( "Error creating node group %s on peer %s", nodeGroup, peer.getName() ),
                        exceptionUtil.getRootCause( e ) );
            }
            results.add( new NodeGroupBuildResult( containers, exception ) );
        }


        return results;
    }
}
