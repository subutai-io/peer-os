package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for Merge
 */
public class UnstashTest extends SystemOutRedirectTest
{
    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    Unstash unstash;


    @Before
    public void setUp()
    {
        unstash = new Unstash( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new Unstash( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        unstash.doExecute();


        verify( gitManager ).unstash( anyString(), anyString() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).unstash( anyString(), anyString() );


        unstash.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }
}
