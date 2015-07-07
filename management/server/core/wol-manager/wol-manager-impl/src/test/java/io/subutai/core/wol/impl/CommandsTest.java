package io.subutai.core.wol.impl;

import org.junit.Before;
import org.junit.Test;
import io.subutai.common.command.RequestBuilder;
import io.subutai.core.wol.impl.Commands;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class CommandsTest
{
    Commands commands;
    RequestBuilder requestBuilder;
    @Before
    public void setUp() throws Exception
    {
        commands = new Commands();
        requestBuilder = mock(RequestBuilder.class);
    }

    @Test
    public void testGetSendWakeOnLanCommand() throws Exception
    {
        commands.getSendWakeOnLanCommand("test");

        assertNotNull(commands.getSendWakeOnLanCommand("test"));

    }
}