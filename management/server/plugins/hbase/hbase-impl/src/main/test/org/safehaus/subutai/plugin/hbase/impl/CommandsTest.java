package org.safehaus.subutai.plugin.hbase.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.OutputRedirection;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CommandsTest
{
    public final static String PACKAGE_NAME = Common.PACKAGE_PREFIX + HBaseConfig.PRODUCT_KEY.toLowerCase();
    Commands commands;
    RequestBuilder requestBuilder_for_GetInstallDialogCommand;
    RequestBuilder requestBuilder_for_GetUninstallCommand;
    RequestBuilder requestBuilder_for_GetStartCommand;
    RequestBuilder requestBuilder_for_GetStopCommand;
    RequestBuilder requestBuilder_for_GetConfigBackupMastersCommand;
    RequestBuilder requestBuilder_for_GetStatusCommand;
    RequestBuilder requestBuilder_for_GetConfigQuorumCommand;
    RequestBuilder requestBuilder_for_GetConfigRegionCommand;
    RequestBuilder requestBuilder_for_GetConfigMasterCommand;
    RequestBuilder requestBuilder_for_GetCheckInstalledCommand;

    @Before
    public void setUp() throws Exception
    {
        requestBuilder_for_GetCheckInstalledCommand = new RequestBuilder("dpkg -l | grep '^ii' | grep " + Common
                .PACKAGE_PREFIX_WITHOUT_DASH);
        requestBuilder_for_GetConfigMasterCommand = new RequestBuilder(
                String.format(". /etc/profile && master.sh %s %s", "test", "test"));
        requestBuilder_for_GetConfigRegionCommand = new RequestBuilder(String.format(". /etc/profile && region.sh " +
                "%s", "test"));
        requestBuilder_for_GetConfigQuorumCommand = new RequestBuilder(String.format(". /etc/profile && quorum.sh " +
                "%s", "test"));
        requestBuilder_for_GetConfigBackupMastersCommand = new RequestBuilder(String.format(". /etc/profile && " +
                "backUpMasters.sh %s", "test"));
        requestBuilder_for_GetStatusCommand = new RequestBuilder("service hbase status");
        requestBuilder_for_GetStopCommand = new RequestBuilder("service hbase stop").withTimeout(360);
        requestBuilder_for_GetStartCommand = new RequestBuilder("service hbase start &");
        requestBuilder_for_GetUninstallCommand = new RequestBuilder("apt-get --force-yes --assume-yes purge " +
                PACKAGE_NAME).withTimeout(360)
                .withStdOutRedirection(
                        OutputRedirection.NO);
        requestBuilder_for_GetInstallDialogCommand = new RequestBuilder("apt-get --assume-yes --force-yes install " +
                "dialog").withTimeout(360)
                .withStdOutRedirection(
                        OutputRedirection.NO);
        commands = new Commands();
    }

    @Test
    public void testGetInstallDialogCommand() throws Exception
    {
        commands.getInstallDialogCommand();

        assertNotNull(commands.getInstallDialogCommand());
        assertEquals(requestBuilder_for_GetInstallDialogCommand, commands.getInstallDialogCommand());

    }

    @Test
    public void testGetUninstallCommand() throws Exception
    {
        commands.getUninstallCommand();

        assertNotNull(commands.getUninstallCommand());
        assertEquals(requestBuilder_for_GetUninstallCommand, commands.getUninstallCommand());

    }

    @Test
    public void testGetStartCommand() throws Exception
    {
        commands.getStartCommand();

        assertNotNull(commands.getStartCommand());
        assertEquals(requestBuilder_for_GetStartCommand, commands.getStartCommand());

    }

    @Test
    public void testGetStopCommand() throws Exception
    {
        commands.getStopCommand();

        assertNotNull(commands.getStopCommand());
        assertEquals(requestBuilder_for_GetStopCommand, commands.getStopCommand());

    }

    @Test
    public void testGetStatusCommand() throws Exception
    {
        commands.getStatusCommand();

        assertNotNull(commands.getStatusCommand());
        assertEquals(requestBuilder_for_GetStatusCommand, commands.getStatusCommand());
    }

    @Test
    public void testGetConfigBackupMastersCommand() throws Exception
    {
        commands.getConfigBackupMastersCommand("test");

        assertNotNull(commands.getConfigBackupMastersCommand("test"));
        assertEquals(requestBuilder_for_GetConfigBackupMastersCommand, commands.getConfigBackupMastersCommand("test"));
    }

    @Test
    public void testGetConfigQuorumCommand() throws Exception
    {
        commands.getConfigQuorumCommand("test");

        assertNotNull(commands.getConfigQuorumCommand("test"));
        assertEquals(requestBuilder_for_GetConfigQuorumCommand, commands.getConfigQuorumCommand("test"));

    }

    @Test
    public void testGetConfigRegionCommand() throws Exception
    {
        commands.getConfigQuorumCommand("test");

        assertNotNull(commands.getConfigRegionCommand("test"));
        assertEquals(requestBuilder_for_GetConfigRegionCommand, commands.getConfigRegionCommand("test"));
    }

    @Test
    public void testGetConfigMasterCommand() throws Exception
    {
        commands.getConfigMasterCommand("test", "test");

        assertNotNull(commands.getConfigMasterCommand("test", "test"));
        assertEquals(requestBuilder_for_GetConfigMasterCommand, commands.getConfigMasterCommand("test", "test"));
    }

    @Test
    public void testGetCheckInstalledCommand() throws Exception
    {
        commands.getCheckInstalledCommand();

        assertNotNull(commands.getCheckInstalledCommand());
        assertEquals(requestBuilder_for_GetCheckInstalledCommand, commands.getCheckInstalledCommand());
    }

//    @Test
//    public void testGetInstallCommand()
//    {
//
//        assertNotNull(commands.getCheckInstalledCommand());
//        assertEquals(requestBuilder_for_GetCheckInstalledCommand, commands.getCheckInstalledCommand());
//
//    }
}