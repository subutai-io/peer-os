package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for AddAll
 */
public class AddAllTest extends SystemOutRedirectTest
{

    private static final String REPOSITORY_ROOT = "repo root";
    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );

    AddAll addAll;


    @Before
    public void setUp() throws Exception
    {
        addAll = new AddAll( gitManager );
        addAll.repoPath = REPOSITORY_ROOT;
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new AddAll( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {
        addAll.doExecute();

        verify( gitManager ).addAll( REPOSITORY_ROOT );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).addAll( REPOSITORY_ROOT );

        addAll.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}
