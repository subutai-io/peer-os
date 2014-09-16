package org.safehaus.subutai.core.environment.cli;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 6/21/14.
 */
@Command(scope = "environment", name = "build", description = "Command to build environment",
        detailedDescription = "Command to build environment by given blueprint description")
public class BuildEnvironmentCommand extends OsgiCommandSupport {

    EnvironmentManager environmentManager;

    @Argument(name = "blueprintStr", description = "Environment blueprint",
            index = 0, multiValued = false, required = true,
            valueToShowInHelp = "Blueprint for building environment")
    private String blueprintStr;
    @Argument(name = "physicalServers", description = "Environment blueprint",
            index = 1, multiValued = true, required = true,
            valueToShowInHelp = "Physical server hostnames")
    private Set<String> physicalServers;


    public EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }


    public void setEnvironmentManager(final EnvironmentManager environmentManager) {
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception {

        EnvironmentBuildTask environmentBuildTask = new EnvironmentBuildTask();
        Set<String> physicalServers = new HashSet<>();


        physicalServers.add(String.valueOf(physicalServers));
//        environmentBuildTask.setEnvironmentBlueprint( blueprintStr );
        boolean buildResult = environmentManager.buildEnvironment(environmentBuildTask);
        if (buildResult) {
            System.out.println("Environment build completed successfully.");
        } else {
            System.out.println("Environment build failed.");
        }
        return null;
    }
}
