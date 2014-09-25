package org.safehaus.subutai.plugin.shark.impl;


import org.junit.BeforeClass;
import org.junit.Test;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.common.mock.CommandRunnerMock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class CommandsTest
{

    private static Commands commands;


    @BeforeClass
    public static void setUp()
    {
        commands = new Commands( new CommandRunnerMock() );
    }


    @Test
    public void testInstallCommand()
    {
        Command command = commands.getInstallCommand( null );

        assertNotNull( command );
        assertEquals( "apt-get --force-yes --assume-yes install ksks-shark", command.getDescription() );
    }


    @Test
    public void testUninstallCommand()
    {
        Command command = commands.getUninstallCommand( null );

        assertNotNull( command );
        assertEquals( "apt-get --force-yes --assume-yes purge ksks-shark", command.getDescription() );
    }


    @Test
    public void testCheckInstalledCommand()
    {
        Command command = commands.getCheckInstalledCommand( null );

        assertNotNull( command );
        assertEquals( "dpkg -l | grep '^ii' | grep ksks", command.getDescription() );
    }
}
