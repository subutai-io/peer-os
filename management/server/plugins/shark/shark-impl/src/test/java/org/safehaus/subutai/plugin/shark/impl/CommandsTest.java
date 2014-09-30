package org.safehaus.subutai.plugin.shark.impl;


import org.junit.BeforeClass;
import org.junit.Test;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.common.mock.CommandRunnerMock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class CommandsTest
{

    @BeforeClass
    public static void setUp()
    {
        Commands.init( new CommandRunnerMock() );
    }


    @Test
    public void testInstallCommand()
    {
        Command command = Commands.getInstallCommand( null );

        assertNotNull( command );
        assertEquals( "apt-get --force-yes --assume-yes install " + Commands.PACKAGE_NAME, command.getDescription() );
    }


    @Test
    public void testUninstallCommand()
    {
        Command command = Commands.getUninstallCommand( null );

        assertNotNull( command );
        assertEquals( "apt-get --force-yes --assume-yes purge " + Commands.PACKAGE_NAME, command.getDescription() );
    }


    @Test
    public void testCheckInstalledCommand()
    {
        Command command = Commands.getCheckInstalledCommand( null );

        assertNotNull( command );
        assertEquals( "dpkg -l | grep '^ii' | grep ksks", command.getDescription() );
    }
}

