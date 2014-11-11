//package org.safehaus.subutai.plugin.zookeeper.impl;
//
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.safehaus.subutai.common.settings.Common;
//import org.safehaus.subutai.core.command.api.command.Command;
//import org.safehaus.subutai.plugin.common.mock.CommandRunnerMock;
//
//import static junit.framework.Assert.assertEquals;
//import static junit.framework.Assert.assertNotNull;
//
//
//public class CommandsTest
//{
//
//    private static Commands commands;
//
//
//    @BeforeClass
//    public static void setUp()
//    {
//        commands = new Commands( new CommandRunnerMock() );
//    }
//
//
//    @Test
//    public void testInstallCommand()
//    {
//        Command command = commands.getInstallCommand( null );
//
//        assertNotNull( command );
//        assertEquals( "apt-get --force-yes --assume-yes install " + Commands.PACKAGE_NAME, command.getDescription() );
//    }
//
//
//    @Test
//    public void getStartCommand()
//    {
//        Command command = commands.getStartCommand( null );
//
//        assertNotNull( command );
//        assertEquals( "service zookeeper start", command.getDescription() );
//    }
//
//
//    @Test
//    public void getRestartCommand()
//    {
//        Command command = commands.getRestartCommand( null );
//
//        assertNotNull( command );
//        assertEquals( "service zookeeper restart", command.getDescription() );
//    }
//
//
//    @Test
//    public void getStatusCommand()
//    {
//        Command command = commands.getStatusCommand( null );
//
//        assertNotNull( command );
//        assertEquals( "service zookeeper status", command.getDescription() );
//    }
//
//
//    @Test
//    public void getStopCommand()
//    {
//        Command command = commands.getStopCommand( null );
//
//        assertNotNull( command );
//        assertEquals( "service zookeeper stop", command.getDescription() );
//    }
//
//
//    @Test
//    public void testUninstallCommand()
//    {
//        Command command = commands.getUninstallCommand( null );
//
//        assertNotNull( command );
//        assertEquals( "apt-get --force-yes --assume-yes purge " + Commands.PACKAGE_NAME, command.getDescription() );
//    }
//
//
//    @Test
//    public void testCheckCommand()
//    {
//        Command command = commands.getCheckInstalledCommand( null );
//
//        assertNotNull( command );
//        assertEquals( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH, command.getDescription() );
//    }
//}
