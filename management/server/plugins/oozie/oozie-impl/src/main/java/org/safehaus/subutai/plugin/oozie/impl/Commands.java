package org.safehaus.subutai.plugin.oozie.impl;


import java.util.Set;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.CommandsSingleton;


public class Commands extends CommandsSingleton
{

    public static final String PACKAGE_NAME = "ksks-oozie-*";
    public static final String SERVER_PACKAGE_NAME = "ksks-oozie-server";
    public static final String CLIENT_PACKAGE_NAME = "ksks-oozie-client";


    public Commands( CommandRunner commandRunner )
    {
        init( commandRunner );
    }


    public static String make( CommandType type )
    {
        switch ( type )
        {
            case STATUS:
                return "dpkg -l | grep '^ii' | grep ksks";
            case INSTALL_SERVER:
                return "apt-get --assume-yes --force-yes install " + SERVER_PACKAGE_NAME;
            case INSTALL_CLIENT:
                return "apt-get --assume-yes --force-yes install " + CLIENT_PACKAGE_NAME;
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


    public static Command getInstallServerCommand( Set<Agent> agents )
    {

        return createCommand(
                new RequestBuilder( "sleep 1; apt-get --force-yes --assume-yes install ksks-oozie-server" )
                        .withTimeout( 180 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }


    public static Command getInstallClientCommand( Set<Agent> agents )
    {

        return createCommand(
                new RequestBuilder( "sleep 1; apt-get --force-yes --assume-yes install ksks-oozie-client" )
                        .withTimeout( 180 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }


    public static Command getStartServerCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service oozie-server start &" ), agents );
    }


    public static Command getStopServerCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service oozie-server stop" ), agents );
    }


    public static Command getStatusServerCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service oozie-server status" ), agents );
    }


    public static Command getConfigureRootHostsCommand( Set<Agent> agents, String param )
    {

        return createCommand( new RequestBuilder( String.format(
                ". /etc/profile && $HADOOP_HOME/bin/hadoop-property.sh add core-site.xml hadoop.proxyuser"
                        + ".root.hosts %s", param ) ), agents );
    }


    public static Command getConfigureRootGroupsCommand( Set<Agent> agents )
    {

        return createCommand( new RequestBuilder( String.format(
                ". /etc/profile && $HADOOP_HOME/bin/hadoop-property.sh add core-site.xml hadoop.proxyuser"
                        + ".root.groups '\\*' " ) ), agents );
    }


    public static Command getUninstallServerCommand( Set<Agent> agents )
    {
        return createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge ksks-oozie-server" ).withTimeout( 90 )
                                                                                                .withStdOutRedirection(
                                                                                                        OutputRedirection.NO ),
                agents );
    }


    public static Command getUninstallClientsCommand( Set<Agent> agents )
    {
        return createCommand(
                new RequestBuilder( "apt-get --force-yes --assume-yes purge ksks-oozie-client" ).withTimeout( 90 )
                                                                                                .withStdOutRedirection(
                                                                                                        OutputRedirection.NO ),
                agents );
    }
}
