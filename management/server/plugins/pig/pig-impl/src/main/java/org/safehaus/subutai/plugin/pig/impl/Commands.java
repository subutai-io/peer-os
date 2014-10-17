package org.safehaus.subutai.plugin.pig.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.plugin.pig.api.PigConfig;


public class Commands
{


    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + PigConfig.PRODUCT_KEY.toLowerCase();

    public final String INSTALL = "apt-get --force-yes --assume-yes install " + PACKAGE_NAME;
    public final String UNINSTALL = "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME;
    public final String CHECK = "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH;

    private final CommandRunnerBase commandRunnerBase;


    public Commands( CommandRunnerBase commandRunnerBase )
    {
        this.commandRunnerBase = commandRunnerBase;
    }


    public Command getInstallCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( INSTALL ).withTimeout( 90 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }


    public Command getUninstallCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( UNINSTALL ).withTimeout( 60 ), agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( CHECK ), agents );
    }
}
