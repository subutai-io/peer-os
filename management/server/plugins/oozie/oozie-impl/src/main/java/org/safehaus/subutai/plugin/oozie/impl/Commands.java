package org.safehaus.subutai.plugin.oozie.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;


public class Commands
{

    public static final String PACKAGE_NAME =
            Common.PACKAGE_PREFIX + OozieClusterConfig.PRODUCT_KEY.toLowerCase() + "-*";
    public static final String SERVER_PACKAGE_NAME =
            Common.PACKAGE_PREFIX + OozieClusterConfig.PRODUCT_KEY.toLowerCase() + "-server";
    public static final String CLIENT_PACKAGE_NAME =
            Common.PACKAGE_PREFIX + OozieClusterConfig.PRODUCT_KEY.toLowerCase() + "-client";


    public Commands()
    {

    }


    public static String make( CommandType type )
    {
        switch ( type )
        {
            case STATUS:
                return "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH;
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


    public RequestBuilder getInstallServerCommand()
    {

        return

                new RequestBuilder( "sleep 1; apt-get --force-yes --assume-yes install " + SERVER_PACKAGE_NAME )
                        .withTimeout( 180 ).withStdOutRedirection( OutputRedirection.NO );
    }


    public RequestBuilder getInstallClientCommand()
    {

        return

                new RequestBuilder( "sleep 1; apt-get --force-yes --assume-yes install " + CLIENT_PACKAGE_NAME )
                        .withTimeout( 180 ).withStdOutRedirection( OutputRedirection.NO );
    }


    public RequestBuilder getStartServerCommand()
    {
        return new RequestBuilder( "service oozie-server start &" );
    }


    public RequestBuilder getStopServerCommand()
    {
        return new RequestBuilder( "service oozie-server stop" );
    }


    public RequestBuilder getStatusServerCommand()
    {
        return new RequestBuilder( "service oozie-server status" );
    }


    public RequestBuilder getConfigureRootHostsCommand( String param )
    {

        return new RequestBuilder( String.format(
                ". /etc/profile && $HADOOP_HOME/bin/hadoop-property.sh add core-site.xml hadoop.proxyuser"
                        + ".root.hosts %s", param ) );
    }


    public RequestBuilder getConfigureRootGroupsCommand()
    {

        return new RequestBuilder( String.format(
                ". /etc/profile && $HADOOP_HOME/bin/hadoop-property.sh add core-site.xml hadoop.proxyuser"
                        + ".root.groups '\\*' " ) );
    }


    public RequestBuilder getUninstallServerCommand()
    {
        return

                new RequestBuilder( "apt-get --force-yes --assume-yes purge " + SERVER_PACKAGE_NAME ).withTimeout( 90 )
                                                                                                     .withStdOutRedirection(
                                                                                                             OutputRedirection.NO )

                ;
    }


    public RequestBuilder getUninstallClientsCommand()
    {
        return

                new RequestBuilder( "apt-get --force-yes --assume-yes purge " + CLIENT_PACKAGE_NAME ).withTimeout( 90 )
                                                                                                     .withStdOutRedirection(
                                                                                                             OutputRedirection.NO )

                ;
    }


    public RequestBuilder getCheckInstalledCommand()
    {
        return new RequestBuilder( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH );
    }
}
