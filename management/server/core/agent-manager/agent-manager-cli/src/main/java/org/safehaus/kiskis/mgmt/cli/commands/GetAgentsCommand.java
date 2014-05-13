package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "agent", name = "get-agents", description = "get the list of agents")
public class GetAgentsCommand extends OsgiCommandSupport {

    protected Object doExecute() {
        System.out.println("get-agents command executed");
        return null;
    }
}
