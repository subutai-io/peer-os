package org.safehaus.subutai.plugin.spark.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.plugin.common.mock.CommandRunnerMock;

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
        assertEquals("sleep 20; apt-get --force-yes --assume-yes install ksks-spark", command.getDescription() );
    }

    @Test
    public void testUninstallCommand() {
        Command command = commands.getUninstallCommand( null );

        assertNotNull(command);
        assertEquals("apt-get --force-yes --assume-yes purge ksks-spark", command.getDescription() );
    }

    @Test
    public void testCheckCommand() {
        Command command = commands.getCheckInstalledCommand( null );

        assertNotNull(command);
        assertEquals("dpkg -l | grep '^ii' | grep ksks", command.getDescription() );
    }


    @Test
    public void testStartAllCommand() {
        Command command = commands.getStartAllCommand( null );

        assertNotNull( command );
        assertEquals( "service spark-all start &", command.getDescription() );
    }

    @Test
    public void testStopAllCommand() {
        Command command = commands.getStopAllCommand( null );

        assertNotNull( command );
        assertEquals( "service spark-all stop", command.getDescription() );
    }

    @Test
    public void testStatusAllCommand() {
        Command command = commands.getStatusAllCommand( null );

        assertNotNull( command );
        assertEquals( "service spark-all status", command.getDescription() );
    }

    @Test
    public void testStartMasterCommand() {
        Command command = commands.getRestartMasterCommand( null );

        assertNotNull( command );
        assertEquals( "service spark-master stop && service spark-master start", command.getDescription() );
    }

    @Test
    public void testStopMasterCommand() {
        Command command = commands.getStopMasterCommand( null );

        assertNotNull( command );
        assertEquals( "service spark-master stop", command.getDescription() );
    }

    @Test
    public void testRestartMasterCommand() {
        Command command = commands.getRestartMasterCommand( null );

        assertNotNull( command );
        assertEquals( "service spark-master stop && service spark-master start", command.getDescription() );
    }

    @Test
    public void testStartSlaveCommand() {
        Command command = commands.getStartSlaveCommand( null );

        assertNotNull( command );
        assertEquals( "service spark-slave start", command.getDescription() );
    }

    @Test
    public void testStopSlaveCommand() {
        Command command = commands.getStopSlaveCommand( null );

        assertNotNull( command );
        assertEquals( "service spark-slave stop", command.getDescription() );
    }

    @Test
    public void testSetMasterIPCommand() {
        Command command = commands.getClearSlavesCommand( null );

        assertNotNull( command );
        assertEquals( ". /etc/profile && sparkSlaveConf.sh clear", command.getDescription() );
    }
}
