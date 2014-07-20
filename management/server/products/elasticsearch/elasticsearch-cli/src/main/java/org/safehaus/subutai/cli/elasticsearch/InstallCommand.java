package org.safehaus.subutai.cli.elasticsearch;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.elasticsearch.Elasticsearch;
import org.safehaus.subutai.shared.protocol.Agent;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "elasticsearch", name = "install" )
public class InstallCommand extends OsgiCommandSupport {

    @Argument(index = 0, name = "hostname", required = true, multiValued = false)
    private String hostname = null;

    private Elasticsearch elasticsearch;

    private AgentManager agentManager;


    public void setElasticsearch( Elasticsearch elasticsearch ) {
        this.elasticsearch = elasticsearch;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }

// status
    protected Object doExecute() {

        AgentResult agentResult = elasticsearch.serviceStatus( agentManager.getAgentByHostname( hostname ) );

        System.out.println( "exitCode: " + agentResult.getExitCode() );
        System.out.println( "stdOut: " + agentResult.getStdOut() );
        System.out.println( "stdErr: " + agentResult.getStdErr() );

        return null;
    }


//  stop
//    protected Object doExecute() {
//
//        AgentResult agentResult = elasticsearch.serviceStop( agentManager.getAgentByHostname( hostname ) );
//
//        System.out.println( "exitCode: " + agentResult.getExitCode() );
//        System.out.println( "stdOut: " + agentResult.getStdOut() );
//        System.out.println( "stdErr: " + agentResult.getStdErr() );
//
//        return null;
//    }


//    start
//    protected Object doExecute() {
//
//        AgentResult agentResult = elasticsearch.serviceStart( agentManager.getAgentByHostname( hostname ) );
//
//        System.out.println( "exitCode: " + agentResult.getExitCode() );
//        System.out.println( "stdOut: " + agentResult.getStdOut() );
//        System.out.println( "stdErr: " + agentResult.getStdErr() );
//
//        return null;
//    }


//    remove
//    protected Object doExecute() {
//
//        AgentResult agentResult = elasticsearch.remove( agentManager.getAgentByHostname( hostname ) );
//
//        System.out.println( "exitCode: " + agentResult.getExitCode() );
//        System.out.println( "stdOut: " + agentResult.getStdOut() );
//        System.out.println( "stdErr: " + agentResult.getStdErr() );
//
//        return null;
//    }


//    install
//    protected Object doExecute() {
//
//        AgentResult agentResult = elasticsearch.install( agentManager.getAgentByHostname( hostname ) );
//        System.out.println( "exitCode: " + agentResult.getExitCode() );
//        System.out.println( "stdOut: " + agentResult.getStdOut() );
//        System.out.println( "stdErr: " + agentResult.getStdErr() );
//
//        return null;
//    }

}
