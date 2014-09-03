package org.safehaus.subutai.core.configuration.cli;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.core.configuration.api.TextInjector;


/**
 * Displays the last log entries
 */
@Command (scope = "config", name = "cat", description = "Executes cat command on given host")
public class CatCommand extends OsgiCommandSupport {

	//    private static AgentManager agentManager;
	private static TextInjector textInjector;
	@Argument (index = 0, name = "hostname", required = true, multiValued = false, description = "Agent hostname")
	String hostname;
	@Argument (index = 1, name = "pathToFile", required = true, multiValued = false, description = "Path to file")
	String pathToFile;


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
		String fileContent = textInjector.catFile(hostname, pathToFile);
		System.out.println(fileContent);


		//        System.out.println( sb.toString() );
		return null;
	}
}
