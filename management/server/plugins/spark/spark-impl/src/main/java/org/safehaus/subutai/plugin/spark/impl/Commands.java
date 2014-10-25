package org.safehaus.subutai.plugin.spark.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import com.google.common.collect.Sets;


public class Commands
{

    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + SparkClusterConfig.PRODUCT_KEY.toLowerCase();
    private final CommandRunnerBase commandRunnerBase;


    public Commands( CommandRunnerBase commandRunnerBase )
    {
        this.commandRunnerBase = commandRunnerBase;
    }


    public Command getInstallCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes install " + PACKAGE_NAME ).withTimeout( 600 )
                                                                                                .withStdOutRedirection(
                                                                                                        OutputRedirection.NO ),
                agents );
    }


    public Command getUninstallCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME ).withTimeout( 600 ),
                agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH ), agents );
    }


    public Command getStartAllCommand( Agent masterNode )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service spark-all start" ).withTimeout( 360 ),
                Sets.newHashSet( masterNode ) );
    }


    public Command getStopAllCommand( Agent masterNode )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service spark-all stop" ).withTimeout( 60 ),
                Sets.newHashSet( masterNode ) );
    }


    public Command getStatusAllCommand( Agent agent )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service spark-all status" ).withTimeout( 60 ),
                Sets.newHashSet( agent ) );
    }


    public Command getStartMasterCommand( Agent masterNode )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service spark-master start" ).withTimeout( 90 ),
                Sets.newHashSet( masterNode ) );
    }


    public Command getRestartMasterCommand( Agent masterNode )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( "service spark-master stop && service spark-master start" ).withTimeout( 60 ),
                Sets.newHashSet( masterNode ) );
    }


    public Command getRestartClusterCommand( Agent masterNode )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( "service spark-all stop && service spark-all start" ).withTimeout( 60 ),
                Sets.newHashSet( masterNode ) );
    }


    public Command getStopMasterCommand( Agent masterNode )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service spark-master stop" ).withTimeout( 60 ),
                Sets.newHashSet( masterNode ) );
    }


    public Command getStatusMasterCommand( Agent masterNode )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service spark-master status" ).withTimeout( 60 ),
                Sets.newHashSet( masterNode ) );
    }


    public Command getStartSlaveCommand( Agent slaveNode )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service spark-slave start" ).withTimeout( 90 ),
                Sets.newHashSet( slaveNode ) );
    }


    public Command getStatusSlaveCommand( Agent slaveNode )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service spark-slave status" ).withTimeout( 90 ),
                Sets.newHashSet( slaveNode ) );
    }


    public Command getStopSlaveCommand( Agent slaveNode )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service spark-slave stop" ).withTimeout( 60 ),
                Sets.newHashSet( slaveNode ) );
    }


    public Command getSetMasterIPCommand( Agent masterNode, Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder(
                String.format( ". /etc/profile && sparkMasterConf.sh clear ; sparkMasterConf.sh %s",
                        masterNode.getHostname() ) ).withTimeout( 60 ), agents );
    }


    public Command getClearSlavesCommand( Agent masterNode )
    {
        return commandRunnerBase
                .createCommand( new RequestBuilder( ". /etc/profile && sparkSlaveConf.sh clear" ).withTimeout( 60 ),
                        Sets.newHashSet( masterNode ) );
    }


    public Command getClearSlaveCommand( Agent slave, Agent masterNode )
    {
        return commandRunnerBase.createCommand( new RequestBuilder(
                String.format( ". /etc/profile && sparkSlaveConf.sh clear %s", slave.getHostname() ) )
                .withTimeout( 60 ), Sets.newHashSet( masterNode ) );
    }


    public Command getAddSlaveCommand( Agent slave, Agent masterNode )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( String.format( ". /etc/profile && sparkSlaveConf.sh %s", slave.getHostname() ) )
                        .withTimeout( 60 ), Sets.newHashSet( masterNode ) );
    }


    public Command getAddSlavesCommand( Set<Agent> slaveNodes, Agent masterNode )
    {
        StringBuilder slaves = new StringBuilder();
        for ( Agent slaveNode : slaveNodes )
        {
            slaves.append( slaveNode.getHostname() ).append( " " );
        }

        return commandRunnerBase.createCommand( new RequestBuilder(
                String.format( ". /etc/profile && sparkSlaveConf.sh clear ; sparkSlaveConf.sh %s", slaves ) )
                .withTimeout( 60 ), Sets.newHashSet( masterNode ) );
    }
}
