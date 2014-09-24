package org.safehaus.subutai.plugin.hbase.impl;


import java.util.Set;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.CommandsSingleton;


public class Commands extends CommandsSingleton
{

    public Commands( CommandRunner commandRunner )
    {
        init( commandRunner );
    }


    public static Command getInstallDialogCommand( Set<Agent> agents )
    {

        return createCommand( new RequestBuilder( "apt-get --assume-yes --force-yes install dialog" ).withTimeout( 360 )
                                                                                                     .withStdOutRedirection(
                                                                                                             OutputRedirection.NO ),
                agents );
    }


    public static Command getInstallCommand( Set<Agent> agents )
    {

        return createCommand(
                new RequestBuilder( "sleep 20; apt-get --assume-yes --force-yes install ksks-hbase" ).withTimeout( 360 )
                                                                                                     .withStdOutRedirection(
                                                                                                             OutputRedirection.NO ),
                agents );
    }


    public static Command getUninstallCommand( Set<Agent> agents )
    {

        return createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge ksks-hbase" ).withTimeout( 360 )
                                                                                         .withStdOutRedirection(
                                                                                                 OutputRedirection.NO ),
                agents );
    }


    public static Command getStartCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service hbase start &" ), agents );
    }


    public static Command getStopCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service hbase stop &" ), agents );
    }


    public static Command getStatusCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service hbase status" ), agents );
    }


    /*public static Command getConfigureCommand( Set<Agent> agents, String param ) {

        return createCommand( new RequestBuilder(
                        String.format( ". /etc/profile && $CASSANDRA_HOME/bin/cassandra-conf.sh %s", param ) ),
                agents );
    }*/


    // $HBASE_HOME/scripts/
    public static Command getConfigBackupMastersCommand( Set<Agent> agents, String hostname )
    {
        return createCommand( new RequestBuilder( String.format( ". /etc/profile && backUpMasters.sh %s", hostname ) ),
                agents );
    }


    //$HBASE_HOME/scripts/
    public static Command getConfigQuorumCommand( Set<Agent> agents, String quorums )
    {
        return createCommand( new RequestBuilder( String.format( ". /etc/profile && quorum.sh %s", quorums ) ),
                agents );
    }


    //$HBASE_HOME/scripts/
    public static Command getConfigRegionCommand( Set<Agent> agents, String hostnames )
    {
        return createCommand( new RequestBuilder( String.format( ". /etc/profile && region.sh %s", hostnames ) ),
                agents );
    }


    //$HBASE_HOME/scripts/
    public static Command getConfigMasterTask( Set<Agent> agents, String hadoopNameNodeHostname,
                                               String hMasterMachineHostname )
    {
        return createCommand( new RequestBuilder(
                String.format( ". /etc/profile && master.sh %s %s", hadoopNameNodeHostname, hMasterMachineHostname ) ),
                agents );
    }
}
