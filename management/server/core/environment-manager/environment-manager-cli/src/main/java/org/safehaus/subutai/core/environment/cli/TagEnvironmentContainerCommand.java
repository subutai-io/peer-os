package org.safehaus.subutai.core.environment.cli;


import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Strings;


@Command( scope = "environment", name = "tag-container" )
public class TagEnvironmentContainerCommand extends OsgiCommandSupport
{

    EnvironmentManager environmentManager;

    @Argument( name = "environmentId", index = 0, required = true, multiValued = false,
            description = "Environment Id", valueToShowInHelp = "Environment Id" )
    private String environmentName;
    @Argument( index = 1, name = "container name", multiValued = false, required = true, description = "container "
            + "name" )
    private String containerName;

    @Argument( index = 2, name = "tag", multiValued = false, required = false, description = "tag" )
    private String tag;


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Environment environment = environmentManager.getEnvironment( environmentName );
        ContainerHost containerHost = environment.getContainerHostByHostname( containerName );
        if ( containerHost == null )
        {
            System.out.println( String.format( "Container %s not found", containerName ) );
        }
        else
        {
            if ( !Strings.isNullOrEmpty( tag ) )
            {
                containerHost.addTag( tag );
            }
            System.out.println( String.format( "Tags are %s", containerHost.getTags() ) );
        }
        return null;
    }
}
