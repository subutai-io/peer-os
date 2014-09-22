package org.safehaus.subutai.plugin.hbase.impl;


import java.util.Set;

import com.google.common.collect.Sets;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.CommandsSingleton;


public class Commands extends CommandsSingleton
{

    protected final static String PACKAGE_NAME = "ksks-hbase";
    protected final static String PACKAGE_PREFIX = "ksks";

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
                new RequestBuilder( "apt-get --assume-yes --force-yes install " + PACKAGE_NAME ).withTimeout( 360 )
                                                                                                     .withStdOutRedirection(
                                                                                                             OutputRedirection.NO ),
                agents );
    }


    public static Command getUninstallCommand( Set<Agent> agents )
    {

        return createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME ).withTimeout( 360 )
                                                                                         .withStdOutRedirection(
                                                                                                 OutputRedirection.NO ),
                agents );
    }


    public static Command getStartCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service hbase start &" ), agents );
    }


    public static Command getStartCluster( Agent hmaster )
    {
        return createCommand( new RequestBuilder( "service hbase start &" ), Sets.newHashSet( hmaster ) );
    }



    public static Command getStopCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service hbase stop &" ), agents );
    }


    public static Command getStatusCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service hbase status" ), agents );
    }


    public static Command getConfigBackupMastersCommand( Set<Agent> agents, String backUpMasters )
    {
        return createCommand( new RequestBuilder( String.format( ". /etc/profile && backUpMasters.sh %s", backUpMasters ) ),
                agents );
    }


    public static Command getConfigQuorumCommand( Set<Agent> agents, String quorumPeers )
    {
        return createCommand( new RequestBuilder( String.format( ". /etc/profile && quorum.sh %s", quorumPeers ) ),
                agents );
    }


    public static Command getConfigRegionCommand( Set<Agent> agents, String regionServers )
    {
        return createCommand( new RequestBuilder( String.format( ". /etc/profile && region.sh %s", regionServers ) ),
                agents );
    }


    public static Command getConfigMasterCommand( Set< Agent > agents, String hadoopNameNodeHostname,
                                                  String hMasterMachineHostname )
    {
        return createCommand( new RequestBuilder(
                String.format( ". /etc/profile && master.sh %s %s", hadoopNameNodeHostname, hMasterMachineHostname ) ),
                agents );
    }

    public static Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "dpkg -l | grep '^ii' | grep " + PACKAGE_PREFIX ), agents );
    }

}
