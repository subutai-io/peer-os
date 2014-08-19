package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command (scope = "hivequery", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

	protected Object doExecute() {
		System.out.println("list clusters command executed");
		return null;
	}
}
