package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for DeleteBranch
 */
public class DeleteBranchTest extends SystemOutRedirectTest
{

    private static final String ERR_MSG = "OOPS";

    private GitManager gitManager = mock( GitManager.class );
    DeleteBranch deleteBranch;


    @Before
    public void setUp()
    {
        deleteBranch = new DeleteBranch( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new DeleteBranch( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        deleteBranch.doExecute();

        verify( gitManager ).deleteBranch( anyString(), anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).deleteBranch( anyString(), anyString() );


        deleteBranch.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}
