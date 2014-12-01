package org.safehaus.subutai.plugin.hive.impl;


import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.peer.api.ContainerHost;


public class Commands
{
    public static final String EXEC_PROFILE = ". /etc/profile";

    public static final String installCommand = "apt-get --force-yes --assume-yes install ";

    public static final String uninstallCommand = "apt-get --force-yes --assume-yes purge ";

    public static final String startCommand = "service hive-thrift start";

    public static final String stopCommand = "service hive-thrift stop";

    public static final String restartCommand = "service hive-thrift restart";

    public static final String statusCommand = "service hive-thrift status";

    public static final String checkIfInstalled = "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH;


    public static String configureHiveServer( String ip )
    {
        return ". /etc/profile && hive-configure.sh " + ip;
    }


    public static String configureClient( ContainerHost server )
    {
        String uri = "thrift://" + server.getIpByInterfaceName( "eth0" ) + ":10000";
        return Commands.addHiveProperty( "add", "hive-site.xml", "hive.metastore.uris", uri );
    }


    public static String addHiveProperty( String cmd, String propFile, String property, String value )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( ". /etc/profile && hive-property.sh " ).append( cmd ).append( " " );
        sb.append( propFile ).append( " " ).append( property );
        if ( value != null )
        {
            sb.append( " " ).append( value );
        }
        return sb.toString();
    }
}
