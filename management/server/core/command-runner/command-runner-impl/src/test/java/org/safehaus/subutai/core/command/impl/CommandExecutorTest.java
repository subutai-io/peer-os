/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.impl;


import org.junit.Test;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.core.command.api.command.CommandExecutor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


/**
 * Test for CommandExecutor class
 */
public class CommandExecutorTest
{

    @Test( expected = NullPointerException.class )
    public void constructorShouldFailNullCommand()
    {
        new CommandExecutor( null, mock( CommandCallback.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailNullCallback()
    {
        new CommandExecutor( mock( CommandImpl.class ), null );
    }


    @Test
    public void constructorShouldPass()
    {
        new CommandExecutor( mock( CommandImpl.class ), mock( CommandCallback.class ) );
    }


    @Test
    public void shouldReturnValidValues()
    {
        CommandImpl command = mock( CommandImpl.class );
        CommandCallback callback = mock( CommandCallback.class );
        CommandExecutor commandExecutor = new CommandExecutor( command, callback );

        assertEquals( command, commandExecutor.getCommand() );
        assertEquals( callback, commandExecutor.getCallback() );
    }
}
