package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.hive.query.HiveQuery;


/**
 * Displays the last log entries
 */
@Command (scope = "hivequery", name = "save-query", description = "Saves hive query in db")
public class SaveQueryCommand extends OsgiCommandSupport {

	@Argument (index = 0, name = "name", description = "The name of query.", required = true, multiValued = false)
	String name = null;
	@Argument (index = 1, name = "query", description = "The sql of hive query.", required = true, multiValued = false)
	String query = null;
	@Argument (index = 2, name = "description", description = "The description of query.", required = true, multiValued = false)
	String description = null;

	// Interface to Hive Query API
	private HiveQuery manager;

	public HiveQuery getManager() {
		return manager;
	}

	public void setManager(HiveQuery manager) {
		this.manager = manager;
	}

	@Override
	protected Object doExecute() throws Exception {
		manager.save(name, query, description);

		return null;
	}
}
