/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.container.impl.lxcmanager;


import java.util.Set;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandsSingleton;

import com.google.common.collect.Sets;


/**
 * Commands for lxc management
 */
public class Commands extends CommandsSingleton
{

    public static Command getCloneCommand( Agent physicalAgent, String lxcHostName )
    {
        return createCommand( new RequestBuilder(
                String.format( "/usr/bin/lxc-clone -o base-container -n %1$s && addhostlxc %1$s", lxcHostName ) )
                .withTimeout( 360 ), Sets.newHashSet( physicalAgent ) );
    }


    public static Command getCloneNStartCommand( Agent physicalAgent, String lxcHostName )
    {
        return createCommand( new RequestBuilder( String.format(
                "/usr/bin/lxc-clone -o base-container -n %1$s && addhostlxc %1$s && /usr/bin/lxc-start -n %1$s -d &",
                lxcHostName ) ).withTimeout( 360 ), Sets.newHashSet( physicalAgent ) );
    }


    public static Command getLxcListCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "/usr/bin/lxc-ls -f" ).withTimeout( 60 ), agents );
    }


    public static Command getLxcInfoCommand( Agent physicalAgent, String lxcHostName )
    {
        return createCommand(
                new RequestBuilder( String.format( "/usr/bin/lxc-info -n %s", lxcHostName ) ).withTimeout( 60 ),
                Sets.newHashSet( physicalAgent ) );
    }


    public static Command getLxcStartCommand( Agent physicalAgent, String lxcHostName )
    {
        return createCommand(
                new RequestBuilder( String.format( "/usr/bin/lxc-start -n %s -d &", lxcHostName ) ).withTimeout( 180 ),
                Sets.newHashSet( physicalAgent ) );
    }


    public static Command getLxcStopCommand( Agent physicalAgent, String lxcHostName )
    {
        return createCommand(
                new RequestBuilder( String.format( "/usr/bin/lxc-stop -n %s &", lxcHostName ) ).withTimeout( 180 ),
                Sets.newHashSet( physicalAgent ) );
    }


    public static Command getLxcDestroyCommand( Agent physicalAgent, String lxcHostName )
    {
        return createCommand( new RequestBuilder( String.format( "/usr/bin/lxc-destroy -n %1$s -f", lxcHostName ) )
                .withTimeout( 180 ), Sets.newHashSet( physicalAgent ) );
    }


    public static Command getMetricsCommand( Set<Agent> agents )
    {
        return createCommand(
                new RequestBuilder( "free -m | grep buffers/cache ; df /lxc-data | grep /lxc-data ; uptime ; nproc" )
                        .withTimeout( 30 ), agents );
    }
}
