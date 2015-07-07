package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for CommitAll
 */
public class CommitAllTest extends SystemOutRedirectTest
{
    private static final String COMMIT_ID = "commit id";
    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    CommitAll commitAll;


    @Before
    public void setUp()
    {
        commitAll = new CommitAll( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new CommitAll( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        when( gitManager.commitAll( anyString(), anyString() ) ).thenReturn( COMMIT_ID );

        commitAll.doExecute();

        verify( gitManager ).commitAll( anyString(), anyString() );
        assertThat( getSysOut(), containsString( COMMIT_ID ) );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).commitAll( anyString(), anyString() );

        commitAll.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}
