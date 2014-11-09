package org.safehaus.subutai.plugin.pig.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.plugin.pig.api.PigConfig;


public class Commands
{


    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + PigConfig.PRODUCT_KEY.toLowerCase();

    public static final String installCommand = "apt-get --force-yes --assume-yes install " + PACKAGE_NAME;
    public static final String uninstallCommand = "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME;
    public static final String checkCommand = "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH;

    public RequestBuilder getInstallCommand(  )
    {
        return new RequestBuilder( installCommand ).withTimeout( 900 );
    }


    public RequestBuilder getUninstallCommand(  )
    {
        return new RequestBuilder( uninstallCommand ).withTimeout( 600 );
    }


    public RequestBuilder getCheckInstalledCommand(  )
    {
        return new RequestBuilder( checkCommand );
    }
}
