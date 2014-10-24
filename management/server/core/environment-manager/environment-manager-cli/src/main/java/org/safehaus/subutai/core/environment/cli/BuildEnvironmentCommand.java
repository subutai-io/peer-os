package org.safehaus.subutai.core.environment.cli;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 6/21/14.
 */
@Command( scope = "environment", name = "build", description = "Command to build environment",
        detailedDescription = "Command to build environment by given blueprint description" )
public class BuildEnvironmentCommand extends OsgiCommandSupport
{

    EnvironmentManager environmentManager;

    @Argument( name = "blueprintStr", description = "Environment blueprint",
            index = 0, multiValued = false, required = true,
            valueToShowInHelp = "Blueprint for building environment" )
    private String blueprintStr;
    @Argument( name = "physicalServers", description = "Environment blueprint",
            index = 1, multiValued = true, required = true,
            valueToShowInHelp = "Physical server hostnames" )
    private Set<String> physicalServers;


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

        Set<String> physicalServers = new HashSet<>();
        physicalServers.add( String.valueOf( physicalServers ) );
        //TODO: code
        return null;
    }
}
