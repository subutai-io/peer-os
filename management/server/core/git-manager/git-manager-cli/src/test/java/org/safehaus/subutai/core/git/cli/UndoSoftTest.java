package org.safehaus.subutai.core.git.cli;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for Merge
 */
public class UndoSoftTest extends SystemOutRedirectTest
{

    private static final String ERR_MSG = "OOPS";
    private static final List<String> FILES = Lists.newArrayList( "file" );
    private GitManager gitManager = mock( GitManager.class );
    UndoSoft undoSoft;


    @Before
    public void setUp()
    {
        undoSoft = new UndoSoft( gitManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new UndoSoft( null );
    }


    @Test
    public void shouldExecuteCommand() throws GitException
    {

        undoSoft.files = FILES;

        undoSoft.doExecute();


        verify( gitManager ).undoSoft( anyString(), anyList() );
    }


    @Test
    public void shouldThrowException() throws GitException
    {
        Mockito.doThrow( new GitException( ERR_MSG ) ).when( gitManager ).undoSoft( anyString(), anyList() );
        undoSoft.files = FILES;


        undoSoft.doExecute();


        assertEquals( ERR_MSG, getSysOut() );
    }
}
