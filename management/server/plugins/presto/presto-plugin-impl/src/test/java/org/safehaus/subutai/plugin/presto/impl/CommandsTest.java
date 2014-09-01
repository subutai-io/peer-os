package org.safehaus.subutai.plugin.presto.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class CommandsTest {

    private static Commands commands;

    @BeforeClass
    public static void setUp() {
        commands = new Commands( new CommandRunnerMock() );
    }


    @Test
    public void testInstallCommand() {
        Command command = commands.getInstallCommand( null );

        assertNotNull(command);
        assertEquals("apt-get --force-yes --assume-yes install ksks-presto", command.getDescription() );
    }

    @Test
    public void getStartCommand() {
        Command command = commands.getStartCommand( null );

        assertNotNull( command );
        assertEquals( "service presto start", command.getDescription() );
    }

    @Test
    public void getRestartCommand() {
        Command command = commands.getRestartCommand( null );

        assertNotNull( command );
        assertEquals( "service presto restart", command.getDescription() );
    }

    @Test
    public void getStatusCommand() {
        Command command = commands.getStatusCommand( null );

        assertNotNull( command );
        assertEquals( "service presto status", command.getDescription() );
    }

    @Test
    public void getStopCommand() {
        Command command = commands.getStopCommand( null );

        assertNotNull( command );
        assertEquals( "service presto stop", command.getDescription() );
    }

    @Test
    public void testUninstallCommand() {
        Command command = commands.getUninstallCommand( null );

        assertNotNull(command);
        assertEquals("apt-get --force-yes --assume-yes purge ksks-presto", command.getDescription() );
    }


    @Test
    public void testCheckCommand() {
        Command command = commands.getCheckInstalledCommand( null );

        assertNotNull(command);
        assertEquals("dpkg -l | grep '^ii' | grep ksks", command.getDescription() );
    }

}
