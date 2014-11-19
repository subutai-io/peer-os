package org.safehaus.subutai.plugin.solr.impl.handler;


import org.junit.BeforeClass;
import org.junit.Test;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.common.mock.CommandRunnerMock;
import org.safehaus.subutai.plugin.solr.impl.Commands;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


/*public class CommandsTest
{

    private static Commands commands;


    @BeforeClass
    public static void setUp()
    {
        commands = new Commands( new CommandRunnerMock() );
    }


    @Test
    public void getStartCommand()
    {
        Command command = commands.getStartCommand( null );

        assertNotNull( command );
        assertEquals( Commands.START, command.getDescription() );
    }


    @Test
    public void getStopCommand()
    {
        Command command = commands.getStopCommand( null );

        assertNotNull( command );
        assertEquals( Commands.STOP, command.getDescription() );
    }


    @Test
    public void getStatusCommand()
    {
        Command command = commands.getStatusCommand( null );

        assertNotNull( command );
        assertEquals( Commands.STATUS, command.getDescription() );
    }
}*/
