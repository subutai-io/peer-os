package org.safehaus.subutai.impl.hbase;


import java.util.Set;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandsSingleton;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.enums.OutputRedirection;


public class Commands extends CommandsSingleton {

    public static Command getInstallDialogCommand( Set<Agent> agents ) {

        return createCommand(
                new RequestBuilder( "apt-get --assume-yes --force-yes install dialog" ).withTimeout( 360 )
                                                                                           .withStdOutRedirection(
                                                                                                   OutputRedirection
                                                                                                           .NO ),
                agents );
    }


    public static Command getInstallCommand( Set<Agent> agents ) {

        return createCommand(
                new RequestBuilder( "sleep 20; apt-get --assume-yes --force-yes install ksks-hbase" ).withTimeout( 360 )
                                                                                           .withStdOutRedirection(
                                                                                                   OutputRedirection
                                                                                                           .NO ),
                agents );
    }


    public static Command getUninstallCommand( Set<Agent> agents ) {

        return createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge ksks-hbase" ).withTimeout( 360 )
                                                                                         .withStdOutRedirection(
                                                                                                 OutputRedirection.NO ),
                agents );
    }


    public static Command getStartCommand( Set<Agent> agents ) {
        return createCommand( new RequestBuilder( "service hbase start &" ), agents );
    }


    public static Command getStopCommand( Set<Agent> agents ) {
        return createCommand( new RequestBuilder( "service hbase stop &" ), agents );
    }


    public static Command getStatusCommand( Set<Agent> agents ) {
        return createCommand( new RequestBuilder( "service hbase status" ), agents );
    }


    public static Command getConfigureCommand( Set<Agent> agents, String param ) {

        return createCommand( new RequestBuilder(
                        String.format( ". /etc/profile && $CASSANDRA_HOME/bin/cassandra-conf.sh %s", param ) ),
                agents );
    }


    public static Command getConfigBackupMastersCommand( Set<Agent> agents, String hostname ) {
        return createCommand( new RequestBuilder(
                        String.format( ". /etc/profile && $HBASE_HOME/scripts/backUpMasters.sh %s", hostname ) ),
                agents );
    }


    public static Command getConfigQuorumCommand( Set<Agent> agents, String quorums ) {
        return createCommand(
                new RequestBuilder( String.format( ". /etc/profile && $HBASE_HOME/scripts/quorum.sh %s", quorums ) ),
                agents );
    }


    public static Command getConfigRegionCommand( Set<Agent> agents, String hostnames ) {
        return createCommand(
                new RequestBuilder( String.format( ". /etc/profile && $HBASE_HOME/scripts/region.sh %s", hostnames ) ),
                agents );
    }


    public static Command getConfigMasterTask( Set<Agent> agents, String hadoopNameNodeHostname,
                                               String hMasterMachineHostname ) {
        return createCommand( new RequestBuilder(
                String.format( ". /etc/profile && $HBASE_HOME/scripts/master.sh %s %s", hadoopNameNodeHostname,
                        hMasterMachineHostname ) ), agents );
    }
}
