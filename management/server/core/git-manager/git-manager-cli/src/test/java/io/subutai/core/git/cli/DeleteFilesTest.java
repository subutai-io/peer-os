package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;
import io.subutai.core.git.cli.DeleteFiles;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for DeleteFiles
 */
public class DeleteFilesTest extends SystemOutRedirectTest
{

    private static final String ERR_MSG = "OOPS";

    private GitManager gitManager = mock( GitManager.class );
    DeleteFiles deleteFiles;


    @Before
    public void setUp()
    {
        deleteFiles = new DeleteFiles( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new DeleteFiles( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        deleteFiles.doExecute();

        verify( gitManager ).delete( anyString(), anyList() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).delete( anyString(), anyList() );


        deleteFiles.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }
}
