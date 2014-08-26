package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.hive.query.Config;
import org.safehaus.subutai.api.hive.query.HiveQuery;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "hivequery", name = "load-query", description = "Loads hive queries from db")
public class LoadQueriesCommand extends OsgiCommandSupport {
	private HiveQuery manager;

	public HiveQuery getManager() {
		return manager;
	}

	public void setManager(HiveQuery manager) {
		this.manager = manager;
	}

	protected Object doExecute() throws Exception {
		List<Config> list = manager.load();
		for (Config query : list) {
			System.out.println(query);
		}

		return null;
	}
}
