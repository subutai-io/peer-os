package org.safehaus.subutai.cli.configpointtracker;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.configpointtracker.ConfigPointTracker;


@Command (scope = "config-point-tracker", name = "remove")
public class RemoveCommand extends OsgiCommandSupport {

	@Argument (index = 0, name = "templateName", required = true)
	private String templateName;

	@Argument (index = 1, name = "configPath", required = true)
	private String configPath;

	private ConfigPointTracker configPointTracker;


	public void setConfigPointTracker(ConfigPointTracker configPointTracker) {
		this.configPointTracker = configPointTracker;
	}


	protected Object doExecute() {

		configPointTracker.remove(templateName, configPath);

		return null;
	}
}
