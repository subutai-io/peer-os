//package org.safehaus.subutai.plugin.presto.impl;
//
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.safehaus.subutai.common.command.RequestBuilder;
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
//        commands = new Commands( );
//    }
//
//
//    @Test
//    public void testInstallCommand()
//    {
//        RequestBuilder command = commands.getInstallCommand();
//
//        assertNotNull( command );
//        assertEquals( new RequestBuilder( "apt-get --force-yes --assume-yes install " + Commands.PACKAGE_NAME ), command );
//    }
//
//
//    @Test
//    public void getStartCommand()
//    {
//        RequestBuilder command = commands.getStartCommand( );
//
//        assertNotNull( command );
//        assertEquals( new RequestBuilder( "service presto start" ), command );
//    }
//
//
//    @Test
//    public void getRestartCommand()
//    {
//        RequestBuilder command = commands.getRestartCommand( );
//
//        assertNotNull( command );
//        assertEquals( new RequestBuilder( "service presto restart" ), command );
//    }
//
//
//    @Test
//    public void getStatusCommand()
//    {
//        RequestBuilder command = commands.getStatusCommand( );
//
//        assertNotNull( command );
//        assertEquals( new RequestBuilder( "service presto status" ), command );
//    }
//
//
//    @Test
//    public void getStopCommand()
//    {
//        RequestBuilder command = commands.getStopCommand( );
//
//        assertNotNull( command );
//        assertEquals( new RequestBuilder( "service presto stop" ), command );
//    }
//
//
//    @Test
//    public void testUninstallCommand()
//    {
//        RequestBuilder command = commands.getUninstallCommand( );
//
//        assertNotNull( command );
//        assertEquals( new RequestBuilder( "apt-get --force-yes --assume-yes purge " + Commands.PACKAGE_NAME ), command );
//    }
//
//
//    @Test
//    public void testCheckCommand()
//    {
//        RequestBuilder command = commands.getCheckInstalledCommand( );
//
//        assertNotNull( command );
//        assertEquals( new RequestBuilder( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH ), command );
//    }
//}
