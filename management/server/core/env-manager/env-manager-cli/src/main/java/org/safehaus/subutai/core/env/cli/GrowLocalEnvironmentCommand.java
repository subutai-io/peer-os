package org.safehaus.subutai.core.env.cli;


import java.util.UUID;

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

        Topology topology = environmentManager.newTopology();
        NodeGroup nodeGroup = environmentManager
                .newNodeGroup( String.format( "NodeGroup%s", System.currentTimeMillis() ), templateName,
                        Common.DEFAULT_DOMAIN_NAME, numberOfContainers, 0, 0, new PlacementStrategy( "ROUND_ROBIN" ) );

        topology.addNodeGroupPlacement( peerManager.getLocalPeer(), nodeGroup );

        environmentManager.growEnvironment( UUID.fromString( environmentId ), topology );

        System.out.println( "Environment is grown " );

        return null;
    }
}
