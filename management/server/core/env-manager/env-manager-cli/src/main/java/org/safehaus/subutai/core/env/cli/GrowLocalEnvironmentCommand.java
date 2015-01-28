package org.safehaus.subutai.core.env.cli;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.api.build.NodeGroup;
import org.safehaus.subutai.core.env.api.build.Topology;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command( scope = "env", name = "grow-local", description = "Command to grow local environment" )
public class GrowLocalEnvironmentCommand extends OsgiCommandSupport
{
    @Argument( name = "envId", description = "Environment id",
            index = 0, multiValued = false, required = true )
    private String environmentId;
    @Argument( name = "templateName", description = "Template name",
            index = 1, multiValued = false, required = true )
    private String templateName;
    @Argument( name = "numberOfContainers", description = "Number of containers",
            index = 2, multiValued = false, required = true )
    private int numberOfContainers;
    @Argument( name = "async", description = "asynchronous build",
            index = 3, multiValued = false, required = false )
    private boolean async = false;

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
                Common.DEFAULT_DOMAIN_NAME, numberOfContainers, 1, 1, new PlacementStrategy( "ROUND_ROBIN" ) );

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
            System.out.println( String.format( "IP %s", containerHost.getIpByInterfaceName( "eth0" ) ) );
            System.out.println( String.format( "Is connected %s", containerHost.isConnected() ) );
        }

        return null;
    }
}
