package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;
import io.subutai.core.git.cli.Merge;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for Merge
 */
public class MergeTest extends SystemOutRedirectTest
{

    private static final String BRANCH_NAME = "branch name";
    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    Merge merge;


    @Before
    public void setUp()
    {
        merge = new Merge( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new Merge( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        merge.doExecute();


        verify( gitManager ).merge( anyString() );
    }


    @Test
    public void shouldExecuteCommand2() throws GitException
    {

        merge.branchName = BRANCH_NAME;


        merge.doExecute();


        verify( gitManager ).merge( anyString(), anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).merge( anyString() );

        merge.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }


    @Test
    public void shouldThrowException2() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).merge( anyString(), anyString() );

        merge.branchName = BRANCH_NAME;


        merge.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }
}
