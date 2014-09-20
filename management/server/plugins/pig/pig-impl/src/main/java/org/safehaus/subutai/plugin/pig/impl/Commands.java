package org.safehaus.subutai.plugin.pig.impl;


import java.util.Set;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.CommandsSingleton;


public class Commands extends CommandsSingleton
{

    public static final String PACKAGE_NAME = "ksks-pig";

    public static final String INSTALL = "apt-get --force-yes --assume-yes install ksks-pig";
    public static final String UNINSTALL = "apt-get --force-yes --assume-yes purge ksks-pig";
    public static final String CHECK = "dpkg -l | grep '^ii' | grep ksks";


    public Commands( CommandRunner commandRunner )
    {
        init( commandRunner );
    }


    public Command getInstallCommand( Set<Agent> agents )
    {
        return createCommand(
                new RequestBuilder( INSTALL ).withTimeout( 90 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }


    public Command getUninstallCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( UNINSTALL ).withTimeout( 60 ), agents );
    }


    public Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( CHECK ), agents );
    }
}
