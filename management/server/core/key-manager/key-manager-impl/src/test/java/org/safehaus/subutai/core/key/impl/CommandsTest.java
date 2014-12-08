package org.safehaus.subutai.core.key.impl;


import org.junit.Test;

import static org.junit.Assert.assertNotNull;


public class CommandsTest
{
    private static final String ARGUMENT = "arg";
    Commands commands = new Commands();


    @Test
    public void testGetCommands() throws Exception
    {
        assertNotNull( commands.getGenerateKeyCommand( ARGUMENT, ARGUMENT ) );
        assertNotNull( commands.getReadKeyCommand( ARGUMENT ) );
        assertNotNull( commands.getReadSshKeyCommand( ARGUMENT ) );
        assertNotNull( commands.getSignCommand( ARGUMENT, ARGUMENT ) );
        assertNotNull( commands.getSendKeyCommand( ARGUMENT ) );
        assertNotNull( commands.getListKeyCommand( ARGUMENT ) );
        assertNotNull( commands.getListKeysCommand() );
        assertNotNull( commands.getDeleteKeyCommand( ARGUMENT ) );
        assertNotNull( commands.getRevokeKeyCommand( ARGUMENT ) );
    }
}
