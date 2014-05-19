/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;


import java.util.Set;

import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandsSingleton;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;


/**
 * Commands for lxc management
 */
public class Commands extends CommandsSingleton {

    public static Command getCloneCommand( Agent physicalAgent, String lxcHostName ) {
        return createCommand( new RequestBuilder(
                        String.format( "/usr/bin/lxc-clone -o base-container -n %1$s && addhostlxc %1$s",
                                lxcHostName ) ).withTimeout( 360 ), Util.wrapAgentToSet( physicalAgent )
                            );
    }


    public static Command getCloneNStartCommand( Agent physicalAgent, String lxcHostName ) {
        return createCommand( new RequestBuilder( String.format(
                        "/usr/bin/lxc-clone -o base-container -n %1$s && addhostlxc %1$s;sleep 10;/usr/bin/lxc-start " +
                                "-n %1$s -d;sleep 10;/usr/bin/lxc-info -n %1$s",
                        lxcHostName ) ).withTimeout( 360 ), Util.wrapAgentToSet( physicalAgent )
                            );
    }


    public static Command getLxcListCommand( Set<Agent> agents ) {
        return createCommand( new RequestBuilder( "/usr/bin/lxc-list" ).withTimeout( 60 ), agents
                            );
    }


    public static Command getLxcInfoCommand( Agent physicalAgent, String lxcHostName ) {
        return createCommand( new RequestBuilder( String.format( "sleep 10 ; /usr/bin/lxc-info -n %s", lxcHostName ) )
                        .withTimeout( 60 ), Util.wrapAgentToSet( physicalAgent )
                            );
    }


    public static Command getLxcStartCommand( Agent physicalAgent, String lxcHostName ) {
        return createCommand(
                new RequestBuilder( String.format( "/usr/bin/lxc-start -n %s -d", lxcHostName ) ).withTimeout( 120 ),
                Util.wrapAgentToSet( physicalAgent )
                            );
    }


    public static Command getLxcStopCommand( Agent physicalAgent, String lxcHostName ) {
        return createCommand(
                new RequestBuilder( String.format( "/usr/bin/lxc-stop -n %s", lxcHostName ) ).withTimeout( 60 ),
                Util.wrapAgentToSet( physicalAgent )
                            );
    }


    public static Command getLxcDestroyCommand( Agent physicalAgent, String lxcHostName ) {
        return createCommand( new RequestBuilder(
                        String.format( "/usr/bin/lxc-stop -n %1$s && /usr/bin/lxc-destroy -n %1$s", lxcHostName ) )
                        .withTimeout( 180 ), Util.wrapAgentToSet( physicalAgent )
                            );
    }


    public static Command getMetricsCommand( Set<Agent> agents ) {
        return createCommand(
                new RequestBuilder( "free -m | grep buffers/cache ; df /dev/sda1 | grep /dev/sda1 ; uptime ; nproc" )
                        .withTimeout( 30 ), agents
                            );
    }
}
