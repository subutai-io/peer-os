package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for Init
 */
public class InitTest extends SystemOutRedirectTest
{

    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    Init init;


    @Before
    public void setUp()
    {
        init = new Init( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new Init( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {
        init.doExecute();

        verify( gitManager ).init( anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).init( anyString() );


        init.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}
