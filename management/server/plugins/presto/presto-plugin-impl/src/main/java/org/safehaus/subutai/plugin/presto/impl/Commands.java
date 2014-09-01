/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.presto.impl;


import com.google.common.collect.Sets;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.commandrunner.CommandsSingleton;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.enums.OutputRedirection;

import java.util.Set;


public class Commands extends CommandsSingleton
{

    public Commands( CommandRunner commandRunner ) {
        init( commandRunner );
    }

    public static Command getInstallCommand( Set<Agent> agents )
    {
        return createCommand(
            new RequestBuilder( "apt-get --force-yes --assume-yes install ksks-presto" )
                .withTimeout( 90 ).withStdOutRedirection( OutputRedirection.NO ),
            agents
        );
    }


    public static Command getUninstallCommand( Set<Agent> agents )
    {
        return createCommand(
            new RequestBuilder( "apt-get --force-yes --assume-yes purge ksks-presto" )
                .withTimeout( 60 ),
            agents
        );
    }


    public static Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return createCommand(
            new RequestBuilder( "dpkg -l | grep '^ii' | grep ksks" ),
            agents );
    }


    public static Command getStartCommand( Set<Agent> agents )
    {
        return createCommand(
            new RequestBuilder( "service presto start" ).withTimeout( 60 ),
            agents );
    }


    public static Command getStopCommand( Set<Agent> agents )
    {
        return createCommand(
            new RequestBuilder( "service presto stop" ).withTimeout( 60 ),
            agents );
    }


    public static Command getRestartCommand( Set<Agent> agents )
    {
        return createCommand(
            new RequestBuilder( "service presto restart" ).withTimeout( 60 ),
            agents );
    }


    public static Command getStatusCommand( Set<Agent> agents )
    {
        return createCommand(
            new RequestBuilder( "service presto status" ),
            agents );
    }


    public static Command getSetCoordinatorCommand( Agent coordinatorNode )
    {
        return createCommand(
            new RequestBuilder( String.format( "presto-config.sh coordinator %s", coordinatorNode.getHostname() ) )
                .withTimeout( 60 ),
            Sets.newHashSet( coordinatorNode )
        );
    }


    public static Command getSetWorkerCommand( Agent coordinatorNode, Set<Agent> agents )
    {
        return createCommand(
            new RequestBuilder( String.format( "presto-config.sh worker %s", coordinatorNode.getHostname() ) )
                .withTimeout( 60 ),
            agents
        );
    }
}
