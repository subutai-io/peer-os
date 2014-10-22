package org.safehaus.subutai.plugin.lucene.impl;


import java.util.Set;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.plugin.lucene.api.LuceneConfig;


public class Commands
{

    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + LuceneConfig.PRODUCT_KEY.toLowerCase();
    public static final String INSTALL = "apt-get --force-yes --assume-yes install " + PACKAGE_NAME;
    public static final String UNINSTALL = "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME;
    public static final String CHECK = "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH;

    private final CommandRunnerBase commandRunner;


    public Commands( CommandRunnerBase commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public Command getUninstallCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( UNINSTALL ).withTimeout( 600 ), agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand( new RequestBuilder( CHECK ), agents );
    }


    public Command getInstallCommand( Set<Agent> agents )
    {
        return commandRunner.createCommand(
                new RequestBuilder( INSTALL ).withTimeout( 900 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }
}
