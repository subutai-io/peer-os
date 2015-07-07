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
 * Test for Merge
 */
public class PullTest extends SystemOutRedirectTest
{

    private static final String BRANCH_NAME = "branch name";
    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    Pull pull;


    @Before
    public void setUp()
    {
        pull = new Pull( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new Pull( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        pull.doExecute();


        verify( gitManager ).pull( anyString() );
    }


    @Test
    public void shouldExecuteCommand2() throws GitException
    {

        pull.branchName = BRANCH_NAME;


        pull.doExecute();


        verify( gitManager ).pull( anyString(), anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).pull( anyString() );


        pull.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }


    @Test
    public void shouldThrowException2() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).pull( anyString(), anyString() );
        pull.branchName = BRANCH_NAME;


        pull.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }
}
