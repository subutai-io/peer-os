package org.safehaus.subutai.plugin.presto.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;

import com.google.common.collect.Sets;


public class Commands
{

    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + PrestoClusterConfig.PRODUCT_KEY.toLowerCase();
    private final CommandRunnerBase commandRunnerBase;


    public Commands( CommandRunnerBase commandRunnerBase )
    {
        this.commandRunnerBase = commandRunnerBase;
    }


    public Command getInstallCommand( Set<Agent> agents )
    {
        RequestBuilder rb = new RequestBuilder( "apt-get --force-yes --assume-yes install " + PACKAGE_NAME );
        return commandRunnerBase
                .createCommand( rb.withTimeout( 600 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }


    public Command getUninstallCommand( Set<Agent> agents )
    {
        RequestBuilder rb = new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME );
        return commandRunnerBase.createCommand( rb.withTimeout( 60 ), agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH ), agents );
    }


    public Command getStartCommand( Set<Agent> agents )
    {
        return commandRunnerBase
                .createCommand( new RequestBuilder( "service presto start" ).withTimeout( 60 ), agents );
    }


    public Command getStopCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service presto stop" ).withTimeout( 60 ), agents );
    }


    public Command getRestartCommand( Set<Agent> agents )
    {
        return commandRunnerBase
                .createCommand( new RequestBuilder( "service presto restart" ).withTimeout( 60 ), agents );
    }


    public Command getStatusCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service presto status" ), agents );
    }


    public Command getSetCoordinatorCommand( Agent coordinatorNode )
    {
        String s = String.format( "presto-config.sh coordinator %s", coordinatorNode.getHostname() );
        return commandRunnerBase
                .createCommand( new RequestBuilder( s ).withTimeout( 60 ), Sets.newHashSet( coordinatorNode ) );
    }


    public Command getSetWorkerCommand( Agent coordinatorNode, Set<Agent> agents )
    {
        String s = String.format( "presto-config.sh worker %s", coordinatorNode.getHostname() );
        return commandRunnerBase.createCommand( new RequestBuilder( s ).withTimeout( 60 ), agents );
    }
}
