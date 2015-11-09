package io.subutai.core.localpeer.impl.command;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandStatus;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CommandResultImplTest
{

    private static final int EXIT_CODE = 0;
    private static final String STD_OUT = "success";
    private static final String STD_ERR = "";
    private static final CommandStatus STATUS = CommandStatus.SUCCEEDED;

    @Mock
    CommandResult commandResult;
    CommandResultImpl commandResultImpl;


    @Before
    public void setUp() throws Exception
    {
        commandResultImpl = new CommandResultImpl( EXIT_CODE, STD_OUT, STD_ERR, STATUS );
        when( commandResult.getExitCode() ).thenReturn( EXIT_CODE );
        when( commandResult.getStdOut() ).thenReturn( STD_OUT );
        when( commandResult.getStdErr() ).thenReturn( STD_ERR );
        when( commandResult.getStatus() ).thenReturn( STATUS );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( EXIT_CODE, ( int ) commandResultImpl.getExitCode() );
        assertEquals( STD_OUT, commandResultImpl.getStdOut() );
        assertEquals( STD_ERR, commandResultImpl.getStdErr() );
        assertEquals( STATUS, commandResultImpl.getStatus() );
        assertTrue( commandResultImpl.hasSucceeded() );
        assertTrue( commandResultImpl.hasCompleted() );
        assertFalse( commandResultImpl.hasTimedOut() );
    }


    @Test
    public void testAdditionalConstructorNProperties() throws Exception
    {
        commandResultImpl = new CommandResultImpl( commandResult );

        assertEquals( EXIT_CODE, ( int ) commandResultImpl.getExitCode() );
        assertEquals( STD_OUT, commandResultImpl.getStdOut() );
        assertEquals( STD_ERR, commandResultImpl.getStdErr() );
        assertEquals( STATUS, commandResultImpl.getStatus() );
        assertTrue( commandResultImpl.hasSucceeded() );
        assertTrue( commandResultImpl.hasCompleted() );
        assertFalse( commandResultImpl.hasTimedOut() );

    }
}
