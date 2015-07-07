package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;
import io.subutai.core.git.cli.RevertCommit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for Merge
 */
public class RevertCommitTest extends SystemOutRedirectTest
{

    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    RevertCommit revertCommit;


    @Before
    public void setUp()
    {
        revertCommit = new RevertCommit( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new RevertCommit( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        revertCommit.doExecute();


        verify( gitManager ).revertCommit( anyString(), anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).revertCommit( anyString(), anyString() );


        revertCommit.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }
}
