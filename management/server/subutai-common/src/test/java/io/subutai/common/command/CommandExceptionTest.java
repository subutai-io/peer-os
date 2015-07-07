package io.subutai.common.command;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.command.CommandException;


@RunWith( MockitoJUnitRunner.class )
public class CommandExceptionTest
{
    private CommandException commandException;

    @Before
    public void setUp() throws Exception
    {
        commandException = new CommandException( new Throwable(  ) );
    }

    @Test
    public void test()
    {

    }
}