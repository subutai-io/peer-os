package org.safehaus.subutai.plugin.accumulo.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class Commands
{

    private static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_NAME.toLowerCase();
    private final CommandRunnerBase commandRunner;


    public static final String EXEC_PROFILE = ". /etc/profile";

    public static final String installCommand = "apt-get --force-yes --assume-yes install ";

    public static final String uninstallCommand = "apt-get --force-yes --assume-yes purge ";

    public static final String startCommand = "/etc/init.d/accumulo start";

    public static final String stopCommand = "/etc/init.d/accumulo stop";

    public static final String restartCommand = "service hive-thrift restart";

    public static final String statusCommand = "service hive-thrift status";

    public static final String checkIfInstalled = "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH;


    public Commands( CommandRunnerBase commandRunner )
    {

        Preconditions.checkNotNull( "Command Runner is null" );

        this.commandRunner = commandRunner;
    }


    public Command getInstallCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes install " + PACKAGE_NAME ).withTimeout( 360 )
                                                                                                .withStdOutRedirection(
                                                                                                        OutputRedirection.NO ),
                agents );
    }


    public Command getUninstallCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME ).withTimeout( 60 ),
                agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        // grep subutai-
        return commandRunner.createCommand(
                new RequestBuilder( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH ), agents );
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


    public static String getAddMasterCommand( String hostname )
    {
        return ". /etc/profile && accumuloMastersConf.sh masters clear && accumuloMastersConf.sh masters add " + hostname;
    }


    public static String getAddTracersCommand( String serializedHostNames )
    {
        return ". /etc/profile && accumuloMastersConf.sh tracers clear && accumuloMastersConf.sh tracers add " + serializedHostNames;
    }


    public static String getClearTracerCommand( String hostname )
    {
        return ". /etc/profile && accumuloMastersConf.sh tracers clear " + hostname;
    }


    public static String getAddGCCommand( String hostname )
    {
        return  ". /etc/profile && accumuloMastersConf.sh gc clear && accumuloMastersConf.sh gc add " + hostname;
    }


    public static String getAddMonitorCommand( String hostname )
    {
        return ". /etc/profile && accumuloMastersConf.sh monitor clear && accumuloMastersConf.sh monitor add " + hostname;
    }


    public static String getAddSlavesCommand( String serializedHostNames )
    {
        return ". /etc/profile && accumuloSlavesConf.sh slaves clear && accumuloSlavesConf.sh slaves add %s" + serializedHostNames;
    }


    public static String getClearSlaveCommand( String hostname )
    {
        return ". /etc/profile && accumuloSlavesConf.sh slaves clear " + hostname;
    }


    public static String getBindZKClusterCommand( String zkNodesCommaSeparated )
    {
        return ". /etc/profile && accumulo-conf.sh remove accumulo-site.xml instance.zookeeper.host && "
                        + "accumulo-conf.sh add accumulo-site.xml instance.zookeeper.host " + zkNodesCommaSeparated;
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


    public static String getInitCommand( String instanceName, String password )
    {
        return ". /etc/profile && accumulo-init.sh " + instanceName  + " " + password;
    }


    public Command getRemoveAccumuloFromHFDSCommand( Agent agent )
    {
        return commandRunner.createCommand( new RequestBuilder( ". /etc/profile && hadoop dfs -rmr /accumulo" ),
                Sets.newHashSet( agent ) );
    }
}
