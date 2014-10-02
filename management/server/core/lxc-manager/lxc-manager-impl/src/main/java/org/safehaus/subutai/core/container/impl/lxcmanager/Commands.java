/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.container.impl.lxcmanager;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Commands for lxc management
 */
public class Commands
{

    private final CommandRunnerBase commandRunnerBase;


    public Commands( final CommandRunnerBase commandRunnerBase )
    {
        Preconditions.checkNotNull( commandRunnerBase, "Command Runner is null" );

        this.commandRunnerBase = commandRunnerBase;
    }


    public Command getCloneCommand( Agent physicalAgent, String lxcHostName )
    {
        return commandRunnerBase.createCommand( new RequestBuilder(
                String.format( "/usr/bin/lxc-clone -o base-container -n %1$s && addhostlxc %1$s", lxcHostName ) )
                .withTimeout( 360 ), Sets.newHashSet( physicalAgent ) );
    }


    public Command getCloneNStartCommand( Agent physicalAgent, String lxcHostName )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( String.format(
                "/usr/bin/lxc-clone -o base-container -n %1$s && addhostlxc %1$s && /usr/bin/lxc-start -n %1$s -d &",
                lxcHostName ) ).withTimeout( 360 ), Sets.newHashSet( physicalAgent ) );
    }


    public Command getLxcListCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "/usr/bin/lxc-ls -f" ).withTimeout( 60 ), agents );
    }


    public Command getLxcInfoCommand( Agent physicalAgent, String lxcHostName )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( String.format( "/usr/bin/lxc-info -n %s", lxcHostName ) ).withTimeout( 60 ),
                Sets.newHashSet( physicalAgent ) );
    }


    public Command getLxcStartCommand( Agent physicalAgent, String lxcHostName )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( String.format( "/usr/bin/lxc-start -n %s -d &", lxcHostName ) ).withTimeout( 180 ),
                Sets.newHashSet( physicalAgent ) );
    }


    public Command getLxcStopCommand( Agent physicalAgent, String lxcHostName )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( String.format( "/usr/bin/lxc-stop -n %s &", lxcHostName ) ).withTimeout( 180 ),
                Sets.newHashSet( physicalAgent ) );
    }


    public Command getLxcDestroyCommand( Agent physicalAgent, String lxcHostName )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( String.format( "/usr/bin/lxc-destroy -n %1$s -f", lxcHostName ) )
                        .withTimeout( 180 ), Sets.newHashSet( physicalAgent ) );
    }


    public Command getMetricsCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( "free -m | grep buffers/cache ; df /lxc-data | grep /lxc-data ; uptime ; nproc" )
                        .withTimeout( 30 ), agents );
    }
}
