/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.jetty.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class Commands
{

    public static final String PACKAGE_NAME = "ksks-jetty";
    private final CommandRunnerBase commandRunner;


    public Commands( CommandRunnerBase commandRunner )
    {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );

        this.commandRunner = commandRunner;
    }


    public Command getInstallCommand( Set<Agent> agents )
    {

        return commandRunner.createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes install " + PACKAGE_NAME ).withTimeout( 360 )
                                                                                                .withStdOutRedirection(
                                                                                                        OutputRedirection.NO ),
                agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "dpkg -l | grep '^ii' | grep ksks" ), agents );
    }


    public Command getStartCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "service jetty start" ), agents );
    }


    public Command getStopCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "service jetty stop" ), agents );
    }


    public Command getStatusCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "service jetty status" ), agents );
    }
}
