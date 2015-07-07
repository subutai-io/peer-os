package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for DiffBranches
 */
public class UndoHardTest extends SystemOutRedirectTest
{

    private static final String BRANCH_NAME = "branch name";
    private static final String ERR_MSG = "OOPS";

    private GitManager gitManager = mock( GitManager.class );
    UndoHard undoHard;


    @Before
    public void setUp()
    {
        undoHard = new UndoHard( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new UndoHard( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        undoHard.doExecute();


        verify( gitManager ).undoHard( anyString() );
    }


    @Test
    public void shouldExecuteCommand2() throws GitException
    {

        undoHard.branchName = BRANCH_NAME;


        undoHard.doExecute();


        verify( gitManager ).undoHard( anyString(), anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).undoHard( anyString() );

        undoHard.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }


    @Test
    public void shouldThrowException2() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).undoHard( anyString(), anyString() );
        undoHard.branchName = BRANCH_NAME;


        undoHard.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }
}
