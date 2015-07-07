package io.subutai.core.git.cli;


import java.io.PrintStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;
import io.subutai.core.git.cli.CommitFiles;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for CommitFiles
 */
public class CommitFilesTest extends SystemOutRedirectTest
{
    private static final String COMMIT_ID = "commit id";
    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    CommitFiles commitFiles;


    @Before
    public void setUp()
    {
        commitFiles = new CommitFiles( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new CommitFiles( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        when( gitManager.commit( anyString(), anyList(), anyString(), anyBoolean() ) ).thenReturn( COMMIT_ID );

        commitFiles.doExecute();

        verify( gitManager ).commit( anyString(), anyList(), anyString(), anyBoolean() );
        assertThat( getSysOut(), containsString( COMMIT_ID ) );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        GitException exception = mock( GitException.class );
        Mockito.doThrow( exception ).when( gitManager )
               .commit( anyString(), any( List.class ), anyString(), anyBoolean() );

        commitFiles.doExecute();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
