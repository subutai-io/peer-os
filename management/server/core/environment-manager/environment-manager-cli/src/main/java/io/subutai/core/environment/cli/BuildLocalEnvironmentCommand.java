package io.subutai.core.environment.cli;


import java.util.UUID;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.protocol.PlacementStrategy;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "environment", name = "build-local", description = "Command to build environment on local peer" )
public class BuildLocalEnvironmentCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "templateName", description = "Template name",
            index = 0, multiValued = false, required = true )
    /**
     * {@value templateName} template to clone for environment hosts
     * {@code required = true}
     */
            String templateName;


    @Argument( name = "numberOfContainers", description = "Number of containers",
            index = 1, multiValued = false, required = true )
    /**
     * {@value numberOfContainers }number of container hosts to create in environment
     * {@code required = true}
     */
            int numberOfContainers;
    @Argument( name = "subnetCidr", description = "Subnet in CIDR notation",
            index = 2, multiValued = false, required = true )
    /**
     * {@value subnetCidr } Subnet in CIDR notation
     * {@code required = true}
     */
            String subnetCidr;

    @Argument( name = "async", description = "asynchronous build",
            index = 3, multiValued = false, required = false )
    /**
     * {@value async} Create environment asynchronously
     * {@code async = false}
     */
            boolean async = false;


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

        final String environmentId = UUID.randomUUID().toString();
        Topology topology = new Topology( "Dummy environment name", environmentId, subnetCidr, null );
        NodeGroup nodeGroup = new NodeGroup( "NodeGroup1", templateName, numberOfContainers, 1, 1,
                new PlacementStrategy( "ROUND_ROBIN" ) );

        topology.addNodeGroupPlacement( peerManager.getLocalPeer(), nodeGroup );

        Environment environment = environmentManager.createEnvironment( topology, async );

        System.out.println( String.format( "Environment created with id %s", environment.getId() ) );

        return null;
    }
}
