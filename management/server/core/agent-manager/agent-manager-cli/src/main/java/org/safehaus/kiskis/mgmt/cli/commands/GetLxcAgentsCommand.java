package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "agent", name = "get-lxc-agents", description = "get lxc agents")
public class GetLxcAgentsCommand extends OsgiCommandSupport {

    protected Object doExecute() {
        System.out.println("get-lxc-agents command executed");
        return null;
    }
}
