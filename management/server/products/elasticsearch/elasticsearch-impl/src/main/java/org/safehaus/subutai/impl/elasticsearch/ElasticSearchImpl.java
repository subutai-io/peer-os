package org.safehaus.subutai.impl.elasticsearch;


import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.elasticsearch.Elasticsearch;
import org.safehaus.subutai.shared.protocol.Agent;

import com.google.common.collect.Sets;


public class ElasticsearchImpl implements Elasticsearch {

    public static final String INSTALL = "apt-get --force-yes --assume-yes install ksks-elasticsearch";

    public static final String REMOVE = "apt-get --force-yes --assume-yes purge ksks-elasticsearch";

    public static final String SERVICE_START = "service elasticsearch start";
    // unrecognized: 256; success: 0;

    public static final String SERVICE_STOP = "service elasticsearch stop";
    // success: 0;

    public static final String SERVICE_STATUS = "service elasticsearch status";
    // not running: 768; running: 0; unrecognized: 256

    public static final String CONFIG = "es-conf.sh %s %s";
//    $ es-conf.sh cluster.name my_cluster
//    $ es-conf.sh node.name my_node1
//    $ es-conf.sh node.master true | false
//    $ es-conf.sh node.data true | false
//    $ es-conf.sh index.number_of_shards 4
//    $ es-conf.sh index.number_of_replicas 2

    private CommandRunner commandRunner;


    public ElasticsearchImpl( CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    @Override
    public AgentResult config( Agent agent, String param, String value ) {

        String configCommand = String.format( CONFIG, param, value );
        System.out.println( "configCommand: " + configCommand );

        Command command = commandRunner.createCommand(
                new RequestBuilder( configCommand ),
                Sets.newHashSet( agent ) );

        commandRunner.runCommand( command );

        return command.getResults().get( agent.getUuid() );
    }


    @Override
    public AgentResult install( Agent agent ) {

        Command command = commandRunner.createCommand(
                new RequestBuilder( INSTALL ),
                Sets.newHashSet( agent ) );

        commandRunner.runCommand( command );

        return command.getResults().get( agent.getUuid() );
    }


    @Override
    public AgentResult remove( Agent agent ) {

        Command command = commandRunner.createCommand(
                new RequestBuilder( REMOVE ),
                Sets.newHashSet( agent ) );

        commandRunner.runCommand( command );

        return command.getResults().get( agent.getUuid() );
    }


    @Override
    public AgentResult serviceStart( Agent agent ) {

        Command command = commandRunner.createCommand(
                new RequestBuilder( SERVICE_START ),
                Sets.newHashSet( agent ) );

        commandRunner.runCommand( command );

        return command.getResults().get( agent.getUuid() );
    }


    @Override
    public AgentResult serviceStop( Agent agent ) {

        Command command = commandRunner.createCommand(
                new RequestBuilder( SERVICE_STOP ),
                Sets.newHashSet( agent ) );

        commandRunner.runCommand( command );

        return command.getResults().get( agent.getUuid() );
    }


    @Override
    public AgentResult serviceStatus( Agent agent ) {

        Command command = commandRunner.createCommand(
                new RequestBuilder( SERVICE_STATUS ),
                Sets.newHashSet( agent ) );

        commandRunner.runCommand( command );

        return command.getResults().get( agent.getUuid() );
    }
}
