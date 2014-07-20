package org.safehaus.subutai.cli.elasticsearch;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.elasticsearch.Elasticsearch;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;


@Command( scope = "elasticsearch", name = "config" )
public class ConfigCommand extends AbstractCommand {

    @Argument( index = 1, name = "param", required = true, multiValued = false )
    private String param = null;

    @Argument( index = 2, name = "value", required = true, multiValued = false )
    private String value = null;


    public void setElasticsearch( Elasticsearch elasticsearch ) {
        this.elasticsearch = elasticsearch;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    protected Object doExecute() {

        AgentResult agentResult = elasticsearch.config( getAgent(), param, value );

        printResult( agentResult );

        return null;
    }
}
