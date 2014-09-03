package org.safehaus.subutai.core.environment.cli;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;


/**
 * Created by bahadyr on 6/21/14.
 */
@Command (scope = "environment", name = "build", description = "Command to build environment",
		detailedDescription = "Command to build environment by given blueprint description")
public class BuildEnvironmentCommand extends OsgiCommandSupport {

	EnvironmentManager environmentManager;

	@Argument (name = "blueprintStr", description = "Environment blueprint",
			index = 0, multiValued = false, required = true,
			valueToShowInHelp = "Blueprint for building environment")
	private String blueprintStr;


	public EnvironmentManager getEnvironmentManager() {
		return environmentManager;
	}


	public void setEnvironmentManager(final EnvironmentManager environmentManager) {
		this.environmentManager = environmentManager;
	}


	@Override
	protected Object doExecute() throws Exception {
		System.out.println("Building environment...");
		boolean buildResult = environmentManager.buildEnvironment(blueprintStr);
		if (buildResult) {
			System.out.println("Environment build completed successfully.");
		} else {
			System.out.println("Environment build failed.");
		}
		return null;
	}
}
