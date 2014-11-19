package org.safehaus.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for Checkout
 */
public class CheckoutTest extends SystemOutRedirectTest
{

    private static final String REPOSITORY_ROOT = "repo root";
    private static final String ERR_MSG = "OOPS";
    private GitManager gitManager = mock( GitManager.class );
    Checkout checkout;


    @Before
    public void setUp()
    {
        checkout = new Checkout( gitManager );
        checkout.repoPath = REPOSITORY_ROOT;
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new Checkout( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        checkout.doExecute();

        verify( gitManager ).checkout( eq( REPOSITORY_ROOT ), anyString(), anyBoolean() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager )
               .checkout( eq( REPOSITORY_ROOT ), anyString(), anyBoolean() );

        checkout.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}
