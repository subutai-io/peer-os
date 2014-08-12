/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.accumulo;


import java.util.Set;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandsSingleton;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.shared.protocol.enums.OutputRedirection;


/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

    public static Command getInstallCommand( Set<Agent> agents ) {
        return createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes install ksks-accumulo" ).withTimeout( 120 )
                                                                                              .withStdOutRedirection(
                                                                                                      OutputRedirection.NO ),
                agents
                            );
    }


    public static Command getUninstallCommand( Set<Agent> agents ) {
        return createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge ksks-accumulo" ).withTimeout( 60 ), agents
                            );
    }


    public static Command getCheckInstalledCommand( Set<Agent> agents ) {
        return createCommand( new RequestBuilder( "dpkg -l | grep '^ii' | grep ksks" ), agents );
    }


    public static Command getStartCommand( Agent agent ) {
        return createCommand( new RequestBuilder( "/etc/init.d/accumulo start" ).withTimeout( 60 ),
                Util.wrapAgentToSet( agent ) );
    }


    public static Command getStopCommand( Agent agent ) {
        return createCommand( new RequestBuilder( "/etc/init.d/accumulo stop" ), Util.wrapAgentToSet( agent ) );
    }


    public static Command getRestartCommand( Agent agent ) {
        return createCommand( new RequestBuilder( "/etc/init.d/accumulo restart" ), Util.wrapAgentToSet( agent ) );
    }


    public static Command getStatusCommand( Agent agent ) {
        return createCommand( new RequestBuilder( "/etc/init.d/accumulo status" ), Util.wrapAgentToSet( agent ) );
    }


    public static Command getAddMasterCommand( Set<Agent> nodes, Agent masterNode ) {
        return createCommand( new RequestBuilder( String.format(
                ". /etc/profile && accumuloMastersConf.sh masters clear && accumuloMastersConf.sh masters add %s",
                masterNode.getHostname() ) ), nodes );
    }


    public static Command getAddTracersCommand( Set<Agent> nodes, Set<Agent> tracerNodes ) {
        StringBuilder tracersSpaceSeparated = new StringBuilder();
        for ( Agent tracer : tracerNodes ) {
            tracersSpaceSeparated.append( tracer.getHostname() ).append( " " );
        }
        return createCommand( new RequestBuilder( String.format(
                ". /etc/profile && accumuloMastersConf.sh tracers clear && accumuloMastersConf.sh tracers add %s",
                tracersSpaceSeparated ) ), nodes );
    }


    public static Command getClearTracerCommand( Set<Agent> nodes, Agent tracerNode ) {
        return createCommand( new RequestBuilder(
                String.format( ". /etc/profile && accumuloMastersConf.sh tracers clear %s",
                        tracerNode.getHostname() ) ), nodes );
    }


    public static Command getAddGCCommand( Set<Agent> nodes, Agent gcNode ) {
        return createCommand( new RequestBuilder(
                String.format( ". /etc/profile && accumuloMastersConf.sh gc clear && accumuloMastersConf.sh gc add %s",
                        gcNode.getHostname() ) ), nodes );
    }


    public static Command getAddMonitorCommand( Set<Agent> nodes, Agent monitor ) {
        return createCommand( new RequestBuilder( String.format(
                ". /etc/profile && accumuloMastersConf.sh monitor clear && accumuloMastersConf.sh monitor add %s",
                monitor.getHostname() ) ), nodes );
    }


    public static Command getAddSlavesCommand( Set<Agent> nodes, Set<Agent> slaveNodes ) {
        StringBuilder slavesSpaceSeparated = new StringBuilder();
        for ( Agent tracer : slaveNodes ) {
            slavesSpaceSeparated.append( tracer.getHostname() ).append( " " );
        }
        return createCommand( new RequestBuilder( String.format(
                ". /etc/profile && accumuloSlavesConf.sh slaves clear && accumuloSlavesConf.sh slaves add %s",
                slavesSpaceSeparated ) ), nodes );
    }


    public static Command getClearSlaveCommand( Set<Agent> nodes, Agent slaveNode ) {
        return createCommand( new RequestBuilder(
                String.format( ". /etc/profile && accumuloSlavesConf.sh slaves clear %s", slaveNode.getHostname() ) ),
                nodes );
    }


    public static Command getBindZKClusterCommand( Set<Agent> nodes, Set<Agent> zkNodes ) {
        StringBuilder zkNodesCommaSeparated = new StringBuilder();
        for ( Agent zkNode : zkNodes ) {
            zkNodesCommaSeparated.append( zkNode.getHostname() ).append( ":2181," );
        }

        zkNodesCommaSeparated.delete( zkNodesCommaSeparated.length() - 1, zkNodesCommaSeparated.length() );
        return createCommand( new RequestBuilder( String.format(
                        ". /etc/profile && accumulo-conf.sh remove accumulo-site.xml instance.zookeeper.host && " +
                                "accumulo-conf.sh add accumulo-site.xml instance.zookeeper.host %s",
                        zkNodesCommaSeparated )
                ), nodes
                            );
    }


    public static Command getAddPropertyCommand( String propertyName, String propertyValue, Set<Agent> agents ) {
        return createCommand( new RequestBuilder(
                String.format( ". /etc/profile && accumulo-property.sh add %s %s", propertyName, propertyValue ) ),
                agents );
    }


    public static Command getRemovePropertyCommand( String propertyName, Set<Agent> agents ) {
        return createCommand(
                new RequestBuilder( String.format( ". /etc/profile && accumulo-property.sh clear %s", propertyName ) ),
                agents );
    }


    public static Command getInitCommand( String instanceName, String password, Agent agent ) {
        return createCommand( new RequestBuilder(
                String.format( ". /etc/profile && accumulo-init.sh %s %s", instanceName, password ) ),
                Util.wrapAgentToSet( agent ) );
    }


    public static Command getRemoveAccumuloFromHFDSCommand( Agent agent ) {
        return createCommand( new RequestBuilder( ". /etc/profile && hadoop dfs -rmr /accumulo" ),
                Util.wrapAgentToSet( agent ) );
    }
}
