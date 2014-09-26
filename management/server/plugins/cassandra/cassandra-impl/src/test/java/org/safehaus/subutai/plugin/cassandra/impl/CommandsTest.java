package org.safehaus.subutai.plugin.cassandra.impl;


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
        Command command = Commands.getInstallCommand( null );

        assertNotNull( command );
        assertEquals( "apt-get --force-yes --assume-yes install " + Commands.PACKAGE_NAME, command.getDescription() );
    }


    @Test
    public void getStartCommand()
    {
        Command command = Commands.getStartCommand( null );

        assertNotNull( command );
        assertEquals( "service cassandra start", command.getDescription() );
    }


    @Test
    public void getStopCommand()
    {
        Command command = Commands.getStopCommand( null );

        assertNotNull( command );
        assertEquals( "service cassandra stop", command.getDescription() );
    }
}
