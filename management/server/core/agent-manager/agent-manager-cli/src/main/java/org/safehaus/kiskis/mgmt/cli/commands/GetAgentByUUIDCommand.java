package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "agent", name = "get-agent-by-uuid", description = "get agent by uuid")
public class GetAgentByUUIDCommand extends OsgiCommandSupport {

    protected Object doExecute() {
        System.out.println("get-agent-by-uuid command executed");
        return null;
    }
}
