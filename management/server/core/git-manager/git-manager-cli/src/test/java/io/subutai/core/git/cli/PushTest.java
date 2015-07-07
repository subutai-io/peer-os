package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;
import io.subutai.core.git.cli.Push;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for Merge
 */
public class PushTest extends SystemOutRedirectTest
{

    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    Push push;


    @Before
    public void setUp()
    {
        push = new Push( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new Push( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {


        push.doExecute();


        verify( gitManager ).push( anyString(), anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).push( anyString(), anyString() );

        push.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }
}
