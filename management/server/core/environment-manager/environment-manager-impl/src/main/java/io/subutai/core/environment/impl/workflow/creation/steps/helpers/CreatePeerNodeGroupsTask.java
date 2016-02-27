package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;

import io.subutai.common.environment.CloneRequest;
import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.environment.CreateEnvironmentContainerGroupResponse;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.ExceptionUtil;


public class CreatePeerNodeGroupsTask implements Callable<CreateEnvironmentContainerGroupResponse>
{
    private static final Logger LOG = LoggerFactory.getLogger( CreatePeerNodeGroupsTask.class );

    private final Peer peer;
    private final Set<NodeGroup> nodeGroups;
    private final LocalPeer localPeer;
    private final Environment environment;
    private final int ipAddressOffset;
    //    private final TemplateManager templateRegistry;
    //    private final String defaultDomain;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();


    public CreatePeerNodeGroupsTask( final Peer peer, final Set<NodeGroup> nodeGroups, final LocalPeer localPeer,
                                     final Environment environment, final int ipAddressOffset/*,
                                     final TemplateManager templateRegistry, final String defaultDomai*/ )
    {
        this.peer = peer;
        this.nodeGroups = nodeGroups;
        this.localPeer = localPeer;
        this.environment = environment;
        this.ipAddressOffset = ipAddressOffset;
        //        this.templateRegistry = templateRegistry;
        //        this.defaultDomain = defaultDomain;
    }


    @Override
    public CreateEnvironmentContainerGroupResponse call() throws Exception
    {
        final Set<NodeGroupBuildResult> results = Sets.newHashSet();
        int currentIpAddressOffset = 0;

        SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils( environment.getSubnetCidr() ).getInfo();
        /*environment.getSubnetCidr().substring( environment.getSubnetCidr().indexOf( "/" ) + 1 );*/
        String maskLength = subnetInfo.getCidrSignature().split( "/" )[1];
        //        Set<EnvironmentContainerImpl> containers = Sets.newHashSet();

        final CreateEnvironmentContainerGroupRequest request = new CreateEnvironmentContainerGroupRequest();
        try
        {
            for ( NodeGroup nodeGroup : nodeGroups )
            {
                LOG.debug( String.format( "Scheduling on %s %s", nodeGroup.getPeerId(), nodeGroup.getName() ) );
                //                ContainerSize containerSize = nodeGroup.getType();

                final String ip = subnetInfo.getAllAddresses()[( ipAddressOffset + currentIpAddressOffset )];

                CloneRequest cloneRequest =
                        new CloneRequest( nodeGroup.getHostId(), nodeGroup.getHostname(), nodeGroup.getName(), ip + "/" + maskLength,
                                environment.getId(), localPeer.getId(), localPeer.getOwnerId(),
                                nodeGroup.getTemplateName(), HostArchitecture.AMD64, nodeGroup.getType() );


                request.addRequest( cloneRequest );

                currentIpAddressOffset++;
                //
                //                for ( ContainerHostInfoModel newHost : response.getHosts() )
                //                {
                //                    containers
                //                            .add( new EnvironmentContainerImpl( localPeer.getId(), peer, newHost
                // .getHostname(), newHost,
                //                                    templateRegistry.getTemplate( nodeGroup.getTemplateName() ),
                //                                    nodeGroup.getSshGroupId(), nodeGroup.getHostsGroupId(),
                // defaultDomain,
                //                                    containerSize, nodeGroup.getHostId(), nodeGroup.getName() ) );
                //                }
                //
                //                if ( containers.isEmpty() )
                //                {
                //                    exception = new NodeGroupBuildException( "Requested container has not been
                // created", null );
                //                }

            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
        return peer.createEnvironmentContainerGroup( request );
    }
}
