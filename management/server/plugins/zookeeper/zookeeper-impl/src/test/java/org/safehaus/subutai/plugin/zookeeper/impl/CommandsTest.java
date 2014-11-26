package org.safehaus.subutai.plugin.zookeeper.impl;


import org.junit.Test;
import org.safehaus.subutai.common.settings.Common;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class CommandsTest
{

    private static Commands commands;


    @Test
    public void testInstallCommand()
    {
        String command = Commands.getInstallCommand();

        assertNotNull( command );
        assertEquals( "apt-get --force-yes --assume-yes install " + Commands.PACKAGE_NAME, command );
    }


    @Test
    public void getStartCommand()
    {
        String command = Commands.getStartCommand();

        assertNotNull( command );
        assertEquals( "service zookeeper start &", command );
    }


    @Test
    public void getRestartCommand()
    {
        String command = Commands.getRestartCommand();

        assertNotNull( command );
        assertEquals( "service zookeeper restart &", command );
    }


    @Test
    public void getStatusCommand()
    {
        String command = Commands.getStatusCommand();

        assertNotNull( command );
        assertEquals( "service zookeeper status", command );
    }


    @Test
    public void getStopCommand()
    {
        String command = Commands.getStopCommand();

        assertNotNull( command );
        assertEquals( "service zookeeper stop", command );
    }


    @Test
    public void testUninstallCommand()
    {
        String command = Commands.getUninstallCommand();

        assertNotNull( command );
        assertEquals( "apt-get --force-yes --assume-yes purge " + Commands.PACKAGE_NAME, command );
    }


    @Test
    public void testCheckCommand()
    {
        String command = Commands.getCheckInstalledCommand();

        assertNotNull( command );
        assertEquals( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH, command );
    }
}
