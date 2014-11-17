package org.safehaus.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import org.safehaus.subutai.core.git.api.GitChangedFile;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for DiffBranches
 */
public class DiffBranchesTest extends SystemOutRedirectTest
{

    private static final String BRANCH_NAME = "branch name";
    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    DiffBranches diffBranches;


    @Before
    public void setUp()
    {
        diffBranches = new DiffBranches( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new DiffBranches( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        when( gitManager.diffBranches( anyString(), anyString() ) )
                .thenReturn( Lists.newArrayList( mock( GitChangedFile.class ) ) );


        diffBranches.doExecute();


        verify( gitManager ).diffBranches( anyString(), anyString() );
    }


    @Test
    public void shouldExecuteCommand2() throws GitException
    {

        diffBranches.branchName2 = BRANCH_NAME;

        diffBranches.doExecute();

        verify( gitManager ).diffBranches( anyString(), anyString(), anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).diffBranches( anyString(), anyString() );

        diffBranches.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }


    @Test
    public void shouldThrowException2() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager )
               .diffBranches( anyString(), anyString(), anyString() );
        diffBranches.branchName2 = BRANCH_NAME;


        diffBranches.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }
}
