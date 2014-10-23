package org.safehaus.subutai.plugin.shark.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;


public class Commands
{

    public static final String PACKAGE_NAME = "ksks-shark";

    private static CommandRunnerBase commandRunner;


    public static void init( CommandRunnerBase commandRunner )
    {
        Commands.commandRunner = commandRunner;
    }


    public static Command getInstallCommand( Set<Agent> agents )
    {
        return createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes install " + PACKAGE_NAME ).withTimeout( 900 )
                                                                                                .withStdOutRedirection(
                                                                                                        OutputRedirection.NO ),
                agents );
    }


    public static Command getUninstallCommand( Set<Agent> agents )
    {
        return createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME ).withTimeout( 600 ),
                agents );
    }


    public static Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "dpkg -l | grep '^ii' | grep ksks" ), agents );
    }


    public static Command getSetMasterIPCommand( Set<Agent> agents, Agent masterNode )
    {
        return createCommand( new RequestBuilder(
                String.format( ". /etc/profile && sharkConf.sh clear master ; sharkConf.sh master %s",
                        masterNode.getHostname() ) ).withTimeout( 60 ), agents );
    }


    private static Command createCommand( RequestBuilder rb, Set<Agent> agents )
    {
        return commandRunner.createCommand( rb, agents );
    }
}

