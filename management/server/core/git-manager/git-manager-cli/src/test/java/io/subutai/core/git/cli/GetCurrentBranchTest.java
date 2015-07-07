package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitBranch;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;
import io.subutai.core.git.cli.GetCurrentBranch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for GetCurrentBranch
 */
public class GetCurrentBranchTest extends SystemOutRedirectTest
{
    private static final GitBranch GIT_BRANCH = new GitBranch( "branch", true );
    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    GetCurrentBranch getCurrentBranch;


    @Before
    public void setUp()
    {
        getCurrentBranch = new GetCurrentBranch( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new GetCurrentBranch( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        when( gitManager.currentBranch( anyString() ) ).thenReturn( GIT_BRANCH );

        getCurrentBranch.doExecute();

        verify( gitManager ).currentBranch( anyString() );
        assertEquals( GIT_BRANCH.toString(), getSysOut() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).currentBranch( anyString() );

        getCurrentBranch.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}
