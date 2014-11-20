package org.safehaus.subutai.plugin.nutch.impl;


import java.util.Set;

import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.command.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;


public class Commands
{

    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + "nutch";
    public static final String INSTALL = "apt-get --force-yes --assume-yes install " + PACKAGE_NAME;
    public static final String UNINSTALL = "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME;
    public static final String CHECK = "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX;

    private final CommandRunnerBase commandRunner;


    public Commands( CommandRunnerBase commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public Command getUninstallCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( UNINSTALL ).withTimeout( 60 ), agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( CHECK ), agents );
    }


    public Command getInstallCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand(
                new RequestBuilder( INSTALL ).withTimeout( 90 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }
}
