package io.subutai.core.env.cli;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.environment.NodeGroup;
import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.UUIDUtil;
import io.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * Adds environment container hosts to target environment
 */
@Command( scope = "env", name = "grow-local", description = "Command to grow local environment" )
public class GrowLocalEnvironmentCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "envId", description = "Environment id",
            index = 0, multiValued = false, required = true )
    /**
     * {@value environmentId} target environment id to grow
     * <p>{@code required = true}</p>
     */
            String environmentId;


    @Argument( name = "templateName", description = "Template name",
            index = 1, multiValued = false, required = true )
    /**
     * {@value templateName} template to clone for new environment container host
     * <p>{@code required = true}</p>
     */
            String templateName;


    @Argument( name = "numberOfContainers", description = "Number of containers",
            index = 2, multiValued = false, required = true )
    /**
     * {@value numberOfContainers} number of containers to add to environment
     * <p>{@code required = true}</p>
     */
            int numberOfContainers;


    @Argument( name = "async", description = "asynchronous build",
            index = 3, multiValued = false, required = false )
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
        Preconditions.checkArgument( UUIDUtil.isStringAUuid( environmentId ), "Invalid environment id" );

        Topology topology = new Topology();
        NodeGroup nodeGroup = new NodeGroup( String.format( "NodeGroup%s", System.currentTimeMillis() ), templateName,
                numberOfContainers, 1, 1, new PlacementStrategy( "ROUND_ROBIN" ) );

        topology.addNodeGroupPlacement( peerManager.getLocalPeer(), nodeGroup );

        Set<ContainerHost> newContainers =
                environmentManager.growEnvironment( UUID.fromString( environmentId ), topology, async );

        System.out.println( "New containers created:" );

        for ( ContainerHost containerHost : newContainers )
        {
            System.out.println( "-----------------------------------------------------------------" );

            System.out.println( String.format( "Container id %s", containerHost.getId() ) );
            System.out.println( String.format( "Container hostname %s", containerHost.getHostname() ) );
            System.out.println( String.format( "Environment id %s", containerHost.getEnvironmentId() ) );
            System.out.println( String.format( "NodeGroup name %s", containerHost.getNodeGroupName() ) );
            System.out.println( String.format( "Template name %s", containerHost.getTemplateName() ) );
            System.out.println( String.format( "IP %s",
                    containerHost.getIpByInterfaceName( Common.DEFAULT_CONTAINER_INTERFACE ) ) );
            System.out.println( String.format( "Is connected %s", containerHost.isConnected() ) );
        }

        return null;
    }
}
