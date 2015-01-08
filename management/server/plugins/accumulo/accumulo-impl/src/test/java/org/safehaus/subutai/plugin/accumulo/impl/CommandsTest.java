package org.safehaus.subutai.plugin.accumulo.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommandsTest
{
    private Commands commands;
    @Before
    public void setUp() throws Exception
    {
        commands = new Commands();
    }

    @Test
    public void test()
    {
        commands.getClearTracerCommand(
                "test");
    }

    @Test
    public void test2()
    {
        commands.getClearSlaveCommand("test");
    }
}