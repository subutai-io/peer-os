/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.jetty.impl;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class Commands
{
    private final int STOP_TIMEOUT = 60 * 1000;
    private final CommandRunnerBase commandRunner;


    public Commands( CommandRunnerBase commandRunner )
    {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );

        this.commandRunner = commandRunner;
    }


    public Command getMakeDirectoryCommand( String directory, Agent agent )
    {
        return commandRunner.createCommand( new RequestBuilder( String.format( "mkdir -p %s", directory ) ),
                Sets.newHashSet( agent ) );
    }


    public Command getPrepareJettyBaseCommand( JettyConfig config, Agent agent )
    {
        return commandRunner.createCommand( new RequestBuilder(
                ". /etc/default/jetty && java -jar $JETTY_HOME/start.jar --add-to-start=deploy,http,logging" )
                .withCwd( config.getBaseDirectory() ), Sets.newHashSet( agent ) );
    }


    public Command getSetJettyBaseVariableCommand( JettyConfig config, Agent agent )
    {
        return commandRunner.createCommand( new RequestBuilder(
                        String.format( "echo \"JETTY_BASE=%s\" >> /etc/default/jetty", config.getBaseDirectory() ) ),
                Sets.newHashSet( agent ) );
    }


    public Command getSetJettyPortVariableCommand( JettyConfig config, Agent agent )
    {
        return commandRunner.createCommand( new RequestBuilder(
                        String.format( "echo \"JETTY_ARGS=jetty.port=%d\" >> /etc/default/jetty", config.getPort() ) ),
                Sets.newHashSet( agent ) );
    }


    public Command getStartCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "service jetty start" ), agents );
    }


    public Command getStopCommand( Set<Agent> agents )
    {
        return commandRunner
                .createCommand( new RequestBuilder( "service jetty stop" ).withTimeout( STOP_TIMEOUT ), agents );
    }


    public Command getStatusCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "service jetty status" ), agents );
    }
}
