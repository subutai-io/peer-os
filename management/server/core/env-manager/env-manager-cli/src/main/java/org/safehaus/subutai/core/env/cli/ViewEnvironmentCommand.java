package org.safehaus.subutai.core.env.cli;


import java.util.UUID;

import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * View target environment brief info
 */
@Command( scope = "env", name = "view", description = "Command to view environment" )
public class ViewEnvironmentCommand extends OsgiCommandSupport
{

    @Argument( name = "envId", description = "Environment id",
            index = 0, multiValued = false, required = true )
    /**
     * {@value environmentId} environment id to view info about
     * <p>{@code required = true}</p>
     */
    private String environmentId;

    private final EnvironmentManager environmentManager;


    public ViewEnvironmentCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Preconditions.checkArgument( UUIDUtil.isStringAUuid( environmentId ), "Invalid environment id" );

        Environment environment = environmentManager.findEnvironment( UUID.fromString( environmentId ) );

        System.out.println( String.format( "Environment name %s", environment.getName() ) );

        for ( ContainerHost containerHost : environment.getContainerHosts() )
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
