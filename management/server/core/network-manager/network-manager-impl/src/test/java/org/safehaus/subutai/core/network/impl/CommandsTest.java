package org.safehaus.subutai.core.network.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import static org.mockito.Mockito.mock;


/**
 * Test for Commands
 */
public class CommandsTest
{
    private CommandDispatcher commandDispatcher;
    private Commands commands;
    private static final UUID AGENT_ID = UUID.randomUUID();
    private static final String HOSTNAME = "hostname";
    private static final String DOMAIN_NAME = "intra.lan";
    private static final String IP = "127.0.0.1";


    @Before
    public void setUp()
    {
        commandDispatcher = mock( CommandDispatcher.class );
        commands = new Commands( commandDispatcher );
    }


    @Test(expected = NullPointerException.class)
    public void constructorShouldFailOnNUllCommandRunner()
    {
        new Commands( null );
    }
}
