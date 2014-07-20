package org.safehaus.subutai.cli.elasticsearch;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.elasticsearch.Elasticsearch;
import org.safehaus.subutai.shared.protocol.Agent;

import org.apache.felix.gogo.commands.Argument;
import org.apache.karaf.shell.console.OsgiCommandSupport;


abstract class AbstractCommand extends OsgiCommandSupport {

    @Argument( index = 0, name = "hostname", required = true, multiValued = false )
    protected String hostname = null;

    protected Elasticsearch elasticsearch;

    protected AgentManager agentManager;


    protected static void printResult( AgentResult agentResult ) {
        System.out.println( "* exitCode: " + agentResult.getExitCode() );
        System.out.println( "* stdOut: " + agentResult.getStdOut() );
        System.out.println( "* stdErr: " + agentResult.getStdErr() );
    }


    protected Agent getAgent() {
        return agentManager.getAgentByHostname( hostname );
    }
}
