package org.safehaus.subutai.plugin.hipi.impl;


import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;


public class CommandFactory
{
    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + "hipi";
    public static final String INSTALL = "apt-get --force-yes --assume-yes install " + PACKAGE_NAME;
    public static final String UNINSTALL = "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME;
    public static final String CHECK =
            "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX.substring( 0, Common.PACKAGE_PREFIX.length() - 1 );


    public static String build( final NodeOperationType status )
    {

        switch ( status )
        {
            case CHECK_INSTALLATION:
                return CHECK;
            case INSTALL:
                return INSTALL;
            case UNINSTALL:
                return UNINSTALL;
            default:
                throw new IllegalArgumentException( "Unsupported operation type" );
        }
    }
}
