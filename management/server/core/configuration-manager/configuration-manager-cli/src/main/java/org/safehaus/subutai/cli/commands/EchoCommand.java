package org.safehaus.subutai.cli.commands;


//import org.safehaus.subutai.api.agentmanager.AgentManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.configuration.manager.api.TextInjector;


/**
 * Displays the last log entries
 */
@Command (scope = "config", name = "echo", description = "Executes cat command on given host")
public class EchoCommand extends OsgiCommandSupport {

	//    private static AgentManager agentManager;
	private static TextInjector textInjector;
	@Argument (index = 0, name = "hostname", required = true, multiValued = false, description = "Agent hostname")
	String hostname;
	@Argument (index = 1, name = "pathToFile", required = true, multiValued = false, description = "Path to file")
	String pathToFile;
	@Argument (index = 2, name = "content", required = true, multiValued = false, description = "File content")
	String content;


	//    public AgentManager getAgentManager() {
	//        return agentManager;
	//    }

	public TextInjector getTextInjector() {
		return textInjector;
	}


	//    public void setAgentManager( AgentManager agentManager ) {
	//        this.agentManager = agentManager;
	//    }


	public void setTextInjector(final TextInjector textInjector) {
		this.textInjector = textInjector;
	}


	protected Object doExecute() {

		//        Agent agent = agentManager.getAgentByHostname( hostname );
		Boolean result = textInjector.echoTextIntoAgent(hostname, pathToFile, content);
		System.out.println(result);


		//        System.out.println( sb.toString() );
		return null;
	}
}
