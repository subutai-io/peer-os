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


/**
 * Test for Merge
 */
public class StashTest extends SystemOutRedirectTest
{

    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    Stash stash;


    @Before
    public void setUp()
    {
        stash = new Stash( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new Stash( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {


        stash.doExecute();


        verify( gitManager ).stash( anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).stash( anyString() );


        stash.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }
}
