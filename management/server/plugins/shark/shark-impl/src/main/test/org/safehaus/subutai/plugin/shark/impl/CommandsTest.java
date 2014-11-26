package org.safehaus.subutai.plugin.shark.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.OutputRedirection;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandsTest {
    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + SharkClusterConfig.PRODUCT_KEY.toLowerCase();

    Commands commands;
    RequestBuilder requestBuilder_for_GetInstallCommand;
    RequestBuilder requestBuilder_for_GetUninstallCommand;
    RequestBuilder requestBuilder_for_GetCheckInstalledCommand;
    ContainerHost containerHost;
    @Before
    public void setUp() throws Exception {
        requestBuilder_for_GetInstallCommand = new RequestBuilder("apt-get --force-yes --assume-yes install " + PACKAGE_NAME ).withTimeout( 900 )
                .withStdOutRedirection(
                        OutputRedirection.NO);
        requestBuilder_for_GetUninstallCommand = new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME ).withTimeout(600);
        requestBuilder_for_GetCheckInstalledCommand = new RequestBuilder( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH );
        commands = new Commands();
        containerHost = mock(ContainerHost.class);
    }

    @Test
    public void testGetInstallCommand() throws Exception {
        commands.getInstallCommand();

        assertNotNull(commands.getInstallCommand());
        assertEquals(requestBuilder_for_GetInstallCommand,commands.getInstallCommand());
    }

    @Test
    public void testGetUninstallCommand() throws Exception {
        commands.getInstallCommand();

        assertNotNull(commands.getUninstallCommand());
        assertEquals(requestBuilder_for_GetUninstallCommand, commands.getUninstallCommand());
    }

    @Test
    public void testGetCheckInstalledCommand() throws Exception {
        commands.getCheckInstalledCommand();

        assertNotNull(commands.getCheckInstalledCommand());
        assertEquals(requestBuilder_for_GetCheckInstalledCommand, commands.getCheckInstalledCommand());
    }

    @Test
    public void testGetSetMasterIPCommand() throws Exception {
        when(containerHost.getHostname()).thenReturn("test");
        commands.getSetMasterIPCommand(containerHost);

        assertEquals("test", containerHost.getHostname());
        assertNotNull(commands.getSetMasterIPCommand(containerHost));
    }
}