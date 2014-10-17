package org.safehaus.subutai.plugin.elasticsearch.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;

import com.google.common.base.Preconditions;


public class Commands
{
    private final CommandRunnerBase commandRunner;
    private static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + ElasticsearchClusterConfiguration.PRODUCT_KEY.toLowerCase();

    public Commands( final CommandRunnerBase commandRunner )
    {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );

        this.commandRunner = commandRunner;
    }


    public Command getInstallCommand( Set<Agent> agents )
    {

        return commandRunner.createCommand(
                new RequestBuilder( "sleep 10; apt-get --force-yes --assume-yes install " + PACKAGE_NAME )
                        .withTimeout( 90 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }


    public Command getUninstallCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( "Uninstall Mahout",
                new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME ).withTimeout( 60 ),
                agents );
    }


    public Command getConfigureCommand( Set<Agent> agents, String param )
    {
        return commandRunner
                .createCommand( new RequestBuilder( String.format( " . /etc/profile && es-conf.sh %s ", param ) ),
                        agents );
    }


    public Command getStatusCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "service elasticsearch status" ), agents );
    }


    public Command getStartCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "service elasticsearch start" ), agents );
    }


    public Command getStopCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "service elasticsearch stop" ), agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( "Check installed subutai packages",
                new RequestBuilder( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH ), agents );
    }
}
