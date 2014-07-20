package org.safehaus.subutai.cli.elasticsearch;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.elasticsearch.Elasticsearch;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;


@Command( scope = "elasticsearch", name = "execute" )
public class ExecuteCommand extends AbstractCommand {

    @Argument( index = 1, name = "command", required = true, multiValued = false )
    private String command = null;


    public void setElasticsearch( Elasticsearch elasticsearch ) {
        this.elasticsearch = elasticsearch;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    protected Object doExecute() {

        AgentResult agentResult = null;

        switch ( command ) {
            case "install":
                agentResult = elasticsearch.install( getAgent() );
                break;
            case "remove":
                agentResult = elasticsearch.remove( getAgent() );
                break;
            case "serviceStart":
                agentResult = elasticsearch.serviceStart( getAgent() );
                break;
            case "serviceStop":
                agentResult = elasticsearch.serviceStop( getAgent() );
                break;
            case "serviceStatus":
                agentResult = elasticsearch.serviceStatus( getAgent() );
                break;
            default:
                System.out.println( "ERROR: Wrong command!" );
                return null;
        }

        printResult( agentResult );

        return null;
    }
}
