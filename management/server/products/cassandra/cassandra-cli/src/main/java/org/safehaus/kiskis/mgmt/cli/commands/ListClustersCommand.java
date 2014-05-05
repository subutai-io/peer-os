package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

    protected Object doExecute() {
        System.out.println("Executing command mycommand");
        return null;
    }
}
