package org.safehaus.subutai.core.executor.impl;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.CommandStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class CommandResultImplTest
{

    private static final Integer EXIT_CODE = 0;
    private static final String STD_OUT = "out";
    private static final String STD_ERR = "err";
    private static final CommandStatus STATUS = CommandStatus.SUCCEEDED;
    CommandResultImpl commandResult;


    @Before
    public void setUp() throws Exception
    {
        commandResult = new CommandResultImpl( EXIT_CODE, STD_OUT, STD_ERR, STATUS );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( EXIT_CODE, commandResult.getExitCode() );
        assertEquals( STD_OUT, commandResult.getStdOut() );
        assertEquals( STD_ERR, commandResult.getStdErr() );
        assertEquals( STATUS, commandResult.getStatus() );

        assertTrue( commandResult.hasSucceeded() );
        assertTrue( commandResult.hasCompleted() );
        assertFalse( commandResult.hasTimedOut() );
    }
}
