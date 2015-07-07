package io.subutai.pluginmanager.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.pluginmanager.impl.Commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class CommandsTest
{
    private Commands commands;


    @Before
    public void setUp() throws Exception
    {
        commands = new Commands();
    }


    @Test
    public void testMakePackageName() throws Exception
    {
        commands.makePackageName( "testPluginName" );
        assertEquals("testPluginName" + Commands.PACKAGE_POSTFIX, commands.makePackageName( "testPluginName" ));
    }


    @Test
    public void testMakeInstallCommand() throws Exception
    {
        assertNotNull( commands.makeInstallCommand( "testPluginName" ));
    }


    @Test
    public void testMakeRemoveCommand() throws Exception
    {
        assertNotNull( commands.makeRemoveCommand( "testPluginName" ));
    }


    @Test
    public void testMakeUpgradeCommand() throws Exception
    {
        assertNotNull( commands.makeUpgradeCommand( "testPluginName" ));
    }


    @Test
    public void testMakeCheckCommand() throws Exception
    {
        assertNotNull( commands.makeCheckCommand());
    }


    @Test
    public void testMakeCheckIfInstalledCommand() throws Exception
    {
        assertNotNull( commands.makeCheckIfInstalledCommand());
    }


    @Test
    public void testMakeListLocalPluginsCommand() throws Exception
    {
        assertNotNull( commands.makeListLocalPluginsCommand());
    }


    @Test
    public void testMakeIsInstalledCommand() throws Exception
    {
        assertNotNull( commands.makeIsInstalledCommand( "testPluginName" ));
    }
}