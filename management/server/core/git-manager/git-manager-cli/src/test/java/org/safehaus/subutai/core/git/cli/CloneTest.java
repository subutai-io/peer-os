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
 * Test for Clone
 */
public class CloneTest extends SystemOutRedirectTest
{

    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    Clone clone;


    @Before
    public void setUp()
    {
        clone = new Clone( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new Clone( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        clone.doExecute();

        verify( gitManager ).clone( anyString(), anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).clone( anyString(), anyString() );

        clone.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}
