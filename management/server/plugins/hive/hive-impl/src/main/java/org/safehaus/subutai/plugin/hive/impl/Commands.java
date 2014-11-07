package org.safehaus.subutai.plugin.hive.impl;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;


public class Commands
{

    public static final String EXEC_PROFILE = ". /etc/profile";

    public static final String installCommand =
            "apt-get --force-yes --assume-yes install ";

    public static final String uninstallCommand =
            "apt-get --force-yes --assume-yes purge ";

    public static final String startCommand = "service hive-thrift start";


    public static final String stopCommand = "service hive-thrift stop";


    public static final String statusCommand = "service hive-thrift status";


    public static final String checkIfInstalled = "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH;


    public static String configureHiveServer( String ip )
    {
        return ". /etc/profile && hive-configure.sh " + ip;
    }


    public static String configureClient( ContainerHost server )
    {
        String uri = "thrift://" + server.getAgent().getListIP().get( 0 ) + ":10000";
        return Commands.addHivePoperty( "add", "hive-site.xml", "hive.metastore.uris", uri );
    }


    public static String addHivePoperty( String cmd, String propFile, String property, String value )
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

    //    public static String make( CommandType type, Product product )
    //    {
    //        StringBuilder sb;
    //        switch ( type )
    //        {
    //            case LIST:
    //                return "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH;
    //            case INSTALL:
    //            case PURGE:
    //                sb = new StringBuilder( "apt-get --force-yes --assume-yes " );
    //                sb.append( type.toString().toLowerCase() ).append( " " );
    //                sb.append( product.getPackageName() );
    //                break;
    //            case STATUS:
    //            case START:
    //            case STOP:
    //            case RESTART:
    //                sb = new StringBuilder();
    //                if ( product.isProfileScriptRun() )
    //                {
    //                    sb.append( EXEC_PROFILE ).append( " && " );
    //                }
    //                sb.append( "service " ).append( product.getServiceName() );
    //                sb.append( " " ).append( type.toString().toLowerCase() );
    //                break;
    //            default:
    //                throw new AssertionError( type.name() );
    //        }
    //        return sb.toString();
    //    }
}
