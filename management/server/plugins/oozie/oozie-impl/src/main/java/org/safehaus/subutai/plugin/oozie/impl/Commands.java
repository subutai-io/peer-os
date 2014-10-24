package org.safehaus.subutai.plugin.oozie.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;


public class Commands
{

    public static final String PACKAGE_NAME = "ksks-oozie-*";
    public static final String SERVER_PACKAGE_NAME = "ksks-oozie-server";
    public static final String CLIENT_PACKAGE_NAME = "ksks-oozie-client";

    private final CommandRunnerBase commandRunnerBase;


    public Commands( CommandRunnerBase commandRunnerBase )
    {
        this.commandRunnerBase = commandRunnerBase;
    }


    public static String make( CommandType type )
    {
        switch ( type )
        {
            case STATUS:
                return "dpkg -l | grep '^ii' | grep ksks";
            case INSTALL_SERVER:
                return "export DEBIAN_FRONTEND=noninteractive && apt-get --assume-yes --force-yes install "
                        + SERVER_PACKAGE_NAME;
            case INSTALL_CLIENT:
                return "export DEBIAN_FRONTEND=noninteractive && apt-get --assume-yes --force-yes install "
                        + CLIENT_PACKAGE_NAME;
            case PURGE:
                StringBuilder sb = new StringBuilder();
                sb.append( "apt-get --force-yes --assume-yes " );
                sb.append( type.toString().toLowerCase() ).append( " " );
                sb.append( PACKAGE_NAME );
                return sb.toString();
            case START:
            case STOP:
                String s = "service oozie-ng " + type.toString().toLowerCase() + " agent";
                if ( type == CommandType.START )
                {
                    s += " &"; // TODO:
                }
                return s;
            default:
                throw new AssertionError( type.name() );
        }
    }


    public Command getInstallServerCommand( Set<Agent> agents )
    {

        return commandRunnerBase.createCommand(
                new RequestBuilder( "sleep 1; apt-get --force-yes --assume-yes install ksks-oozie-server" )
                        .withTimeout( 1800 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }


    public Command getInstallClientCommand( Set<Agent> agents )
    {

        return commandRunnerBase.createCommand(
                new RequestBuilder( "sleep 1; apt-get --force-yes --assume-yes install ksks-oozie-client" )
                        .withTimeout( 1800 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }


    public Command getStartServerCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service oozie-server start &" ), agents );
    }


    public Command getStopServerCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service oozie-server stop" ), agents );
    }


    public Command getStatusServerCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( "service oozie-server status" ), agents );
    }


    public Command getConfigureRootHostsCommand( Set<Agent> agents, String param )
    {

        return commandRunnerBase.createCommand( new RequestBuilder( String.format(
                ". /etc/profile && $HADOOP_HOME/bin/hadoop-property.sh add core-site.xml hadoop.proxyuser"
                        + ".root.hosts %s", param ) ), agents );
    }


    public Command getConfigureRootGroupsCommand( Set<Agent> agents )
    {

        return commandRunnerBase.createCommand( new RequestBuilder( String.format(
                ". /etc/profile && $HADOOP_HOME/bin/hadoop-property.sh add core-site.xml hadoop.proxyuser"
                        + ".root.groups '\\*' " ) ), agents );
    }


    public Command getUninstallServerCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge ksks-oozie-server" ).withTimeout( 90 )
                                                                                                .withStdOutRedirection(
                                                                                                        OutputRedirection.NO ),
                agents );
    }


    public Command getUninstallClientsCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge ksks-oozie-client" ).withTimeout( 90 )
                                                                                                .withStdOutRedirection(
                                                                                                        OutputRedirection.NO ),
                agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( "Check installed ksks packages",
                new RequestBuilder( "dpkg -l | grep '^ii' | grep ksks" ), agents );
    }
}
