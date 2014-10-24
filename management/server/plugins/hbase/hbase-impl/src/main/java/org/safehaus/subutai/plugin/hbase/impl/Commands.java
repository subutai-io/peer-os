package org.safehaus.subutai.plugin.hbase.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;


public class Commands
{

    protected final static String PACKAGE_NAME = Common.PACKAGE_PREFIX + HBaseClusterConfig.PRODUCT_KEY.toLowerCase();
    private final CommandRunnerBase commandRunner;


    public Commands( CommandRunnerBase commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public Command getInstallDialogCommand( Set<Agent> agents )
    {

        return commandRunner.createCommand(
                new RequestBuilder( "apt-get --assume-yes --force-yes install dialog" ).withTimeout( 360 )
                                                                                       .withStdOutRedirection(
                                                                                               OutputRedirection.NO ),
                agents );
    }


    public Command getInstallCommand( Set<Agent> agents )
    {

        return commandRunner.createCommand(
                new RequestBuilder( "apt-get --assume-yes --force-yes install " + PACKAGE_NAME ).withTimeout( 360 )
                                                                                                .withStdOutRedirection(
                                                                                                        OutputRedirection.NO ),
                agents );
    }


    public Command getUninstallCommand( Set<Agent> agents )
    {

        return commandRunner.createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME ).withTimeout( 360 )
                                                                                              .withStdOutRedirection(
                                                                                                      OutputRedirection.NO ),
                agents );
    }


    public Command getStartCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "service hbase start &" ), agents );
    }


    public Command getStopCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "service hbase stop" ).withTimeout( 360 ), agents );
    }


    public Command getStatusCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( "service hbase status" ), agents );
    }


    public Command getConfigBackupMastersCommand( Set<Agent> agents, String backUpMasters )
    {
        return commandRunner.createCommand(
                new RequestBuilder( String.format( ". /etc/profile && backUpMasters.sh %s", backUpMasters ) ), agents );
    }


    public Command getConfigQuorumCommand( Set<Agent> agents, String quorumPeers )
    {
        return commandRunner
                .createCommand( new RequestBuilder( String.format( ". /etc/profile && quorum.sh %s", quorumPeers ) ),
                        agents );
    }


    public Command getConfigRegionCommand( Set<Agent> agents, String regionServers )
    {
        return commandRunner
                .createCommand( new RequestBuilder( String.format( ". /etc/profile && region.sh %s", regionServers ) ),
                        agents );
    }


    public Command getConfigMasterCommand( Set<Agent> agents, String hadoopNameNodeHostname,
                                           String hMasterMachineHostname )
    {
        return commandRunner.createCommand( new RequestBuilder(
                        String.format( ". /etc/profile && master.sh %s %s", hadoopNameNodeHostname,
                                hMasterMachineHostname ) ), agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand(
                new RequestBuilder( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH ), agents );
    }
}
