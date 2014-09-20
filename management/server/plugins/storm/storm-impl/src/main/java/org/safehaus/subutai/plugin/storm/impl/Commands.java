package org.safehaus.subutai.plugin.storm.impl;


public class Commands
{

    public static final String PACKAGE_NAME = "ksks-storm";
    private static final String EXEC_PROFILE = ". /etc/profile";


    public static String make( CommandType type )
    {
        return make( type, null );
    }


    public static String make( CommandType type, StormService service )
    {
        StringBuilder sb = null;
        switch ( type )
        {
            case LIST:
                return "dpkg -l | grep '^ii' | grep ksks";
            case INSTALL:
            case PURGE:
                sb = new StringBuilder( EXEC_PROFILE );
                // NODE: fix related to apt-get update
                if ( type == CommandType.INSTALL )
                {
                    sb.append( " && sleep 20" );
                }
                sb.append( " && apt-get --force-yes --assume-yes " );
                sb.append( type.toString().toLowerCase() ).append( " " );
                sb.append( PACKAGE_NAME );
                break;
            case STATUS:
            case START:
            case STOP:
            case RESTART:
                if ( service != null )
                {
                    sb = new StringBuilder();
                    sb.append( "service " ).append( service.getService() );
                    sb.append( " " ).append( type.toString().toLowerCase() );
                }
                break;
            default:
                throw new AssertionError( type.name() );
        }
        return sb != null ? sb.toString() : null;
    }


    public static String configure( String cmd, String propFile, String property, String value )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( EXEC_PROFILE ).append( " && storm-property.sh " ).append( cmd );
        sb.append( " " ).append( propFile ).append( " " ).append( property );
        if ( value != null )
        {
            sb.append( " " ).append( value );
        }

        return sb.toString();
    }
}
