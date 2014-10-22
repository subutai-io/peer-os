/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.impl;


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

    private final CommandRunnerBase commandRunner;


    public Commands( CommandRunnerBase commandRunner )
    {

        Preconditions.checkNotNull( "Command Runner is null" );

        this.commandRunner = commandRunner;
    }


    public Command getInstallCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes install ksks-accumulo" ).withTimeout( 600 )
                                                                                              .withStdOutRedirection(
                                                                                                      OutputRedirection.NO ),
                agents );
    }


    public Command getUninstallCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge ksks-accumulo" ).withTimeout( 600 ),
                agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "dpkg -l | grep '^ii' | grep ksks" ), agents );
    }


    public Command getStartCommand( Agent agent )
    {
        return commandRunner.createCommand( new RequestBuilder( "/etc/init.d/accumulo start" ).withTimeout( 60 ),
                Sets.newHashSet( agent ) );
    }


    public Command getStopCommand( Agent agent )
    {
        return commandRunner
                .createCommand( new RequestBuilder( "/etc/init.d/accumulo stop" ), Sets.newHashSet( agent ) );
    }


    public Command getRestartCommand( Agent agent )
    {
        return commandRunner
                .createCommand( new RequestBuilder( "/etc/init.d/accumulo restart" ), Sets.newHashSet( agent ) );
    }


    public Command getStatusCommand( Agent agent )
    {
        return commandRunner
                .createCommand( new RequestBuilder( "/etc/init.d/accumulo status" ), Sets.newHashSet( agent ) );
    }


    public Command getAddMasterCommand( Set<Agent> nodes, Agent masterNode )
    {
        return commandRunner.createCommand( new RequestBuilder( String.format(
                ". /etc/profile && accumuloMastersConf.sh masters clear && accumuloMastersConf.sh masters add %s",
                masterNode.getHostname() ) ), nodes );
    }


    public Command getAddTracersCommand( Set<Agent> nodes, Set<Agent> tracerNodes )
    {
        StringBuilder tracersSpaceSeparated = new StringBuilder();
        for ( Agent tracer : tracerNodes )
        {
            tracersSpaceSeparated.append( tracer.getHostname() ).append( " " );
        }
        return commandRunner.createCommand( new RequestBuilder( String.format(
                ". /etc/profile && accumuloMastersConf.sh tracers clear && accumuloMastersConf.sh tracers add %s",
                tracersSpaceSeparated ) ), nodes );
    }


    public Command getClearTracerCommand( Set<Agent> nodes, Agent tracerNode )
    {
        return commandRunner.createCommand( new RequestBuilder(
                String.format( ". /etc/profile && accumuloMastersConf.sh tracers clear %s",
                        tracerNode.getHostname() ) ), nodes );
    }


    public Command getAddGCCommand( Set<Agent> nodes, Agent gcNode )
    {
        return commandRunner.createCommand( new RequestBuilder(
                String.format( ". /etc/profile && accumuloMastersConf.sh gc clear && accumuloMastersConf.sh gc add %s",
                        gcNode.getHostname() ) ), nodes );
    }


    public Command getAddMonitorCommand( Set<Agent> nodes, Agent monitor )
    {
        return commandRunner.createCommand( new RequestBuilder( String.format(
                ". /etc/profile && accumuloMastersConf.sh monitor clear && accumuloMastersConf.sh monitor add %s",
                monitor.getHostname() ) ), nodes );
    }


    public Command getAddSlavesCommand( Set<Agent> nodes, Set<Agent> slaveNodes )
    {
        StringBuilder slavesSpaceSeparated = new StringBuilder();
        for ( Agent tracer : slaveNodes )
        {
            slavesSpaceSeparated.append( tracer.getHostname() ).append( " " );
        }
        return commandRunner.createCommand( new RequestBuilder( String.format(
                ". /etc/profile && accumuloSlavesConf.sh slaves clear && accumuloSlavesConf.sh slaves add %s",
                slavesSpaceSeparated ) ), nodes );
    }


    public Command getClearSlaveCommand( Set<Agent> nodes, Agent slaveNode )
    {
        return commandRunner.createCommand( new RequestBuilder(
                        String.format( ". /etc/profile && accumuloSlavesConf.sh slaves clear %s",
                                slaveNode.getHostname() ) ), nodes );
    }


    public Command getBindZKClusterCommand( Set<Agent> nodes, Set<Agent> zkNodes )
    {
        StringBuilder zkNodesCommaSeparated = new StringBuilder();
        for ( Agent zkNode : zkNodes )
        {
            zkNodesCommaSeparated.append( zkNode.getHostname() ).append( ":2181," );
        }

        zkNodesCommaSeparated.delete( zkNodesCommaSeparated.length() - 1, zkNodesCommaSeparated.length() );
        return commandRunner.createCommand( new RequestBuilder( String.format(
                ". /etc/profile && accumulo-conf.sh remove accumulo-site.xml instance.zookeeper.host && "
                        + "accumulo-conf.sh add accumulo-site.xml instance.zookeeper.host %s",
                zkNodesCommaSeparated ) ), nodes );
    }


    public Command getAddPropertyCommand( String propertyName, String propertyValue, Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder(
                        String.format( ". /etc/profile && accumulo-property.sh add %s %s", propertyName,
                                propertyValue ) ), agents );
    }


    public Command getRemovePropertyCommand( String propertyName, Set<Agent> agents )
    {
        return commandRunner.createCommand(
                new RequestBuilder( String.format( ". /etc/profile && accumulo-property.sh clear %s", propertyName ) ),
                agents );
    }


    public Command getInitCommand( String instanceName, String password, Agent agent )
    {
        return commandRunner.createCommand( new RequestBuilder(
                        String.format( ". /etc/profile && accumulo-init.sh %s %s", instanceName, password ) ),
                Sets.newHashSet( agent ) );
    }


    public Command getRemoveAccumuloFromHFDSCommand( Agent agent )
    {
        return commandRunner.createCommand( new RequestBuilder( ". /etc/profile && hadoop dfs -rmr /accumulo" ),
                Sets.newHashSet( agent ) );
    }
}
