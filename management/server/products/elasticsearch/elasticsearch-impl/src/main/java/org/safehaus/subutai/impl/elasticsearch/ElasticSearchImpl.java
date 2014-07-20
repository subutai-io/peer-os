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

    // success: 0; unrecognized: 256;
    public static final String SERVICE_START = "service elasticsearch start";

    // success: 0;
    public static final String SERVICE_STOP = "service elasticsearch stop";

    // running: 0; not running: 768; unrecognized: 256;
    public static final String SERVICE_STATUS = "service elasticsearch status";

    public static final String CONFIG = "es-conf.sh %s %s";

    private CommandRunner commandRunner;


    public ElasticsearchImpl( CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    @Override
    public AgentResult config( Agent agent, String param, String value ) {

        String configCommand = String.format( CONFIG, param, value );

        return runCommand( agent, configCommand );
    }


    @Override
    public AgentResult install( Agent agent ) {
        return runCommand( agent, INSTALL );
    }


    @Override
    public AgentResult remove( Agent agent ) {
        return runCommand( agent, REMOVE );
    }


    @Override
    public AgentResult serviceStart( Agent agent ) {
        return runCommand( agent, SERVICE_START );
    }


    @Override
    public AgentResult serviceStop( Agent agent ) {
        return runCommand( agent, SERVICE_STOP );
    }


    @Override
    public AgentResult serviceStatus( Agent agent ) {
        return runCommand( agent, SERVICE_STATUS );
    }


    private AgentResult runCommand( Agent agent, String commandLine ) {

        Command command = commandRunner.createCommand(
                new RequestBuilder( commandLine ),
                Sets.newHashSet( agent ) );

        commandRunner.runCommand( command );

        return command.getResults().get( agent.getUuid() );
    }

}
