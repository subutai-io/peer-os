package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "agent", name = "get-agent-by-hostname", description = "get agent by hostname")
public class GetAgentByHostnameCommand extends OsgiCommandSupport {

    protected Object doExecute() {
        System.out.println("get-agent-by-hostname command executed");
        return null;
    }
}
