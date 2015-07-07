package io.subutai.core.git.cli;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.git.api.GitException;
import io.subutai.core.git.api.GitManager;
import io.subutai.core.git.cli.ListStashes;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for ListStashes
 */
public class ListStashesTest extends SystemOutRedirectTest
{

    private static final String ERR_MSG = "OOPS";
    private static final String STASH_NAME = "stash name";
    private GitManager gitManager = mock( GitManager.class );
    ListStashes listStashes;


    @Before
    public void setUp()
    {
        listStashes = new ListStashes( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullGitManager()
    {
        new ListStashes( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {
        when( gitManager.listStashes( anyString() ) ).thenReturn( Lists.newArrayList( STASH_NAME ) );

        listStashes.doExecute();

        verify( gitManager ).listStashes( anyString() );

        assertEquals( STASH_NAME, getSysOut() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).listStashes( anyString() );

        listStashes.doExecute();

        assertEquals( ERR_MSG, getSysOut() );
    }
}
