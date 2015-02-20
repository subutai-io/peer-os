package org.safehaus.subutai.core.env.cli;


import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.environment.NodeGroup;
import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Karaf console command support Command to build environment on local peer
 */
@Command( scope = "env", name = "build-local", description = "Command to build environment on local peer" )
public class BuildLocalEnvironmentCommand extends OsgiCommandSupport
{


    @Argument( name = "templateName", description = "Template name",
            index = 0, multiValued = false, required = true )
    /**
     * {@value templateName} template to clone for environment hosts
     * {@code required = true}
     */ private String templateName;


    @Argument( name = "numberOfContainers", description = "Number of containers",
            index = 1, multiValued = false, required = true )
    /**
     * {@value numberOfContainers }number of container hosts to create in environment
     * {@code required = true}
     */ private int numberOfContainers;
    @Argument( name = "subnetCidr", description = "Subnet in CIDR notation",
            index = 2, multiValued = false, required = true )
    /**
     * {@value subnetCidr } Subnet in CIDR notation
     * {@code required = true}
     */ private String subnetCidr;

    @Argument( name = "async", description = "asynchronous build",
            index = 3, multiValued = false, required = false )
    /**
     * {@value async} Create environment asynchronously
     * {@code async = false}
     */ private boolean async = false;

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
        Topology topology = new Topology();
        NodeGroup nodeGroup = new NodeGroup( "NodeGroup1", templateName, numberOfContainers, 1, 1,
                new PlacementStrategy( "ROUND_ROBIN" ) );

        topology.addNodeGroupPlacement( peerManager.getLocalPeer(), nodeGroup );

        Environment environment =
                environmentManager.createEnvironment( "Dummy environment name", topology, subnetCidr, null, async );

        System.out.println( String.format( "Environment created with id %s", environment.getId() ) );

        return null;
    }
}
