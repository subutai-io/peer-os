package org.safehaus.subutai.plugin.pig.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;


public class Commands
{


    public static final String PACKAGE_NAME = "ksks-pig";

    public final String INSTALL = "apt-get --force-yes --assume-yes install ksks-pig";
    public final String UNINSTALL = "apt-get --force-yes --assume-yes purge ksks-pig";
    public final String CHECK = "dpkg -l | grep '^ii' | grep ksks";

    private final CommandRunnerBase commandRunnerBase;


    public Commands( CommandRunnerBase commandRunnerBase )
    {
        this.commandRunnerBase = commandRunnerBase;
    }


    public Command getInstallCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand(
                new RequestBuilder( INSTALL ).withTimeout( 900 ).withStdOutRedirection( OutputRedirection.NO ),
                agents );
    }


    public Command getUninstallCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( UNINSTALL ).withTimeout( 600 ), agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return commandRunnerBase.createCommand( new RequestBuilder( CHECK ), agents );
    }
}
