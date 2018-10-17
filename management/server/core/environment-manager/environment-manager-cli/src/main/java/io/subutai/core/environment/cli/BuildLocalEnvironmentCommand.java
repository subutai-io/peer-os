package io.subutai.core.environment.cli;


import java.util.Random;
import java.util.Set;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.Node;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.bazaar.share.quota.ContainerSize;


@Command( scope = "environment", name = "build-local", description = "Command to build environment on local peer" )
public class BuildLocalEnvironmentCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "templateName", description = "Template name", required = true )
    private String templateName;


    @Argument( name = "async", description = "asynchronous build", index = 3 )
    private boolean async = false;


    private final EnvironmentManager environmentManager;
    private final PeerManager peerManager;


    public BuildLocalEnvironmentCommand( final EnvironmentManager environmentManager, final PeerManager peerManager )
    {
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( peerManager );

        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        String peerId = peerManager.getLocalPeer().getId();
        final Set<ResourceHost> resourceHosts = peerManager.getLocalPeer().getResourceHosts();

        if ( resourceHosts.isEmpty() )
        {
            System.out.println( "There are no resource hosts to build environment" );
            return null;
        }
        String hostId = resourceHosts.iterator().next().getId();
        String containerName = String.format( "Container%d", new Random().nextInt( 999 ) );

        Node node =
                new Node( containerName, containerName, ContainerSize.getDefaultContainerQuota( ContainerSize.TINY ),
                        peerId, hostId, peerManager.getLocalPeer().getTemplateByName( templateName ).getId() );

        Topology topology = new Topology( "Dummy environment name" );
        topology.addNodePlacement( peerId, node );

        environmentManager.createEnvironment( topology, async );

        System.out.println( "Environment creation started" );

        return null;
    }
}
