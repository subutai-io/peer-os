package io.subutai.core.environment.cli;


import java.util.Random;
import java.util.Set;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.bazaar.share.quota.ContainerSize;


/**
 * Adds environment container host to target environment
 */
@Command( scope = "environment", name = "grow-local", description = "Command to grow local environment" )
public class GrowLocalEnvironmentCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "envId", description = "Environment id", index = 0, multiValued = false, required = true )
    /**
     * {@value environmentId} target environment id to grow
     * <p>{@code required = true}</p>
     */
            String environmentId;


    @Argument( name = "templateName", description = "Template name", index = 1, multiValued = false, required = true )
    /**
     * {@value templateName} template to clone for new environment container host
     * <p>{@code required = true}</p>
     */
            String templateName;


    @Argument( name = "async", description = "asynchronous build", index = 3, multiValued = false, required = false )
    /**
     * {@value async} grow environment asynchronously
     * <p>{@code required = false}, {@code default = false}</p>
     */
            boolean async = false;

    private final EnvironmentManager environmentManager;
    private final PeerManager peerManager;


    public GrowLocalEnvironmentCommand( final EnvironmentManager environmentManager, final PeerManager peerManager )
    {
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( peerManager );

        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        String peerId = peerManager.getLocalPeer().getId();
        final Set<ResourceHost> resourceHosts = peerManager.getLocalPeer().getResourceHosts();

        if ( resourceHosts.isEmpty() )
        {
            System.out.println( "There are no resource hosts to build environment" );
            return null;
        }
        String hostId = resourceHosts.iterator().next().getId();
        Environment environment = environmentManager.loadEnvironment( environmentId );
        String containerName = String.format( "Container%d", new Random().nextInt( 999 ) );

        Node node =
                new Node( containerName, containerName, ContainerSize.getDefaultContainerQuota( ContainerSize.TINY ),
                        peerId, hostId, peerManager.getLocalPeer().getTemplateByName( templateName ).getId() );

        Topology topology = new Topology( environment.getName() );
        topology.addNodePlacement( peerId, node );


        environmentManager.modifyEnvironment( environmentId, topology, null, null, async );

        System.out.println( "Environment creation started" );


        return null;
    }
}
