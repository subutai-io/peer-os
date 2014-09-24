package org.safehaus.subutai.plugin.presto.impl;


import java.util.Set;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.CommandsSingleton;

import com.google.common.collect.Sets;


public class Commands extends CommandsSingleton
{

    public static final String PACKAGE_NAME = "ksks-presto";


    public Commands( CommandRunner commandRunner )
    {
        init( commandRunner );
    }


    public static Command getInstallCommand( Set<Agent> agents )
    {
        RequestBuilder rb = new RequestBuilder( "apt-get --force-yes --assume-yes install " + PACKAGE_NAME );
        return createCommand( rb.withTimeout( 600 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }


    public static Command getUninstallCommand( Set<Agent> agents )
    {
        RequestBuilder rb = new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME );
        return createCommand( rb.withTimeout( 60 ), agents );
    }


    public static Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "dpkg -l | grep '^ii' | grep ksks" ), agents );
    }


    public static Command getStartCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service presto start" ).withTimeout( 60 ), agents );
    }


    public static Command getStopCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service presto stop" ).withTimeout( 60 ), agents );
    }


    public static Command getRestartCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service presto restart" ).withTimeout( 60 ), agents );
    }


    public static Command getStatusCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service presto status" ), agents );
    }


    public static Command getSetCoordinatorCommand( Agent coordinatorNode )
    {
        String s = String.format( "presto-config.sh coordinator %s", coordinatorNode.getHostname() );
        return createCommand( new RequestBuilder( s ).withTimeout( 60 ), Sets.newHashSet( coordinatorNode ) );
    }


    public static Command getSetWorkerCommand( Agent coordinatorNode, Set<Agent> agents )
    {
        String s = String.format( "presto-config.sh worker %s", coordinatorNode.getHostname() );
        return createCommand( new RequestBuilder( s ).withTimeout( 60 ), agents );
    }
}
