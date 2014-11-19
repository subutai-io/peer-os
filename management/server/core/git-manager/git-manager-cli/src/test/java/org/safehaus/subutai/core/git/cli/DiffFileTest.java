package org.safehaus.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for DiffFile
 */
public class DiffFileTest extends SystemOutRedirectTest
{

    private static final String BRANCH_NAME = "branch name";
    private static final String ERR_MSG = "OOPS";
    private static final String FILE_DIFF = "file diff";
    private GitManager gitManager = mock( GitManager.class );
    DiffFile diffFile;


    @Before
    public void setUp()
    {
        diffFile = new DiffFile( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new DiffFile( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        when( gitManager.diffFile( anyString(), anyString(), anyString() ) ).thenReturn( FILE_DIFF );


        diffFile.doExecute();


        verify( gitManager ).diffFile( anyString(), anyString(), anyString() );
    }


    @Test
    public void shouldExecuteCommand2() throws GitException
    {

        diffFile.branchName2 = BRANCH_NAME;

        diffFile.doExecute();

        verify( gitManager ).diffFile( anyString(), anyString(), anyString(), anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager )
               .diffFile( anyString(), anyString(), anyString() );

        diffFile.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }


    @Test
    public void shouldThrowException2() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager )
               .diffFile( anyString(), anyString(), anyString(), anyString() );

        diffFile.branchName2 = BRANCH_NAME;


        diffFile.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }
}
