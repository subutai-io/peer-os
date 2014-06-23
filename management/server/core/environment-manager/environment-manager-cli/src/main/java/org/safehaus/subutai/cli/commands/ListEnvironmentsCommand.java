package org.safehaus.subutai.cli.commands;


import java.util.Set;

import org.safehaus.subutai.api.manager.Environment;
import org.safehaus.subutai.api.manager.EnvironmentManager;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 6/21/14.
 */
@Command(scope = "environment", name = "ls", description = "Command to build environment",
        detailedDescription = "Command to build environment by given blueprint description")
public class ListEnvironmentsCommand extends OsgiCommandSupport {

    EnvironmentManager environmentManager;


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager ) {
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception {
        Set<Environment> environments = environmentManager.getEnvironments();
        for(Environment environment : environments) {
            System.out.println(environment.getName());
        }

        return null;
    }
}
