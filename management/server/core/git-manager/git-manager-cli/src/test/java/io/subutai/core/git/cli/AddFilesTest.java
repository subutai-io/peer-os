package io.subutai.core.git.cli;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for AddFiles
 */
public class AddFilesTest extends SystemOutRedirectTest
{

    private static final String REPOSITORY_ROOT = "repo root";
    private static final List<String> FILES = Lists.newArrayList( "file" );
    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );

    AddFiles addFiles;


    @Before
    public void setUp() throws Exception
    {
        addFiles = new AddFiles( gitManager );
        addFiles.repoPath = REPOSITORY_ROOT;
        addFiles.files = FILES;
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new AddFiles( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {
        addFiles.doExecute();

        verify( gitManager ).add( REPOSITORY_ROOT, FILES );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).add( REPOSITORY_ROOT, FILES );

        addFiles.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}
