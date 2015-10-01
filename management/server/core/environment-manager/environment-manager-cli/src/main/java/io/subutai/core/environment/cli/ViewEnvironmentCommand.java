package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * View target environment brief info
 */
@Command( scope = "environment", name = "view", description = "Command to view environment" )
public class ViewEnvironmentCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "envId", description = "Environment id",
            index = 0, multiValued = false, required = true )
    /**
     * {@value environmentId} environment id to view info about
     * <p>{@code required = true}</p>
     */
            String environmentId;

    private final EnvironmentManager environmentManager;


    public ViewEnvironmentCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        Environment environment = environmentManager.loadEnvironment( environmentId );

        System.out.println( String.format( "Environment name %s", environment.getName() ) );

        for ( EnvironmentContainerHost containerHost : environment.getContainerHosts() )
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
