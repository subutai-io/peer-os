package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitBranch;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;
import io.subutai.core.git.cli.ListBranches;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for ListBranches
 */
public class ListBranchesTest extends SystemOutRedirectTest
{

    private static final String ERR_MSG = "OOPS";
    private static final GitBranch GIT_BRANCH = new GitBranch( "branch", true );
    private GitManager gitManager = mock( GitManager.class );
    ListBranches listBranches;


    @Before
    public void setUp()
    {
        listBranches = new ListBranches( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new ListBranches( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        when( gitManager.listBranches( anyString(), anyBoolean() ) ).thenReturn( Lists.newArrayList( GIT_BRANCH ) );

        listBranches.doExecute();

        verify( gitManager ).listBranches( anyString(), anyBoolean() );

        assertEquals( GIT_BRANCH.toString(), getSysOut() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).listBranches( anyString(), anyBoolean() );

        listBranches.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}
