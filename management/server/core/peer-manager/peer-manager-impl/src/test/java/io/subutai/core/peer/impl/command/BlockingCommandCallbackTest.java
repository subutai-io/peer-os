package io.subutai.core.peer.impl.command;


import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.Response;

import io.subutai.core.peer.impl.command.BlockingCommandCallback;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class BlockingCommandCallbackTest
{
    @Mock
    Semaphore completionSemaphore;
    @Mock
    CommandCallback callback;
    @Mock
    Response response;
    @Mock
    CommandResult commandResult;

    BlockingCommandCallback blockingCommandCallback;


    @Before
    public void setUp() throws Exception
    {
        blockingCommandCallback = new BlockingCommandCallback( callback );
        when( commandResult.hasCompleted() ).thenReturn( true );
    }


    @Test
    public void testGetCompletionSemaphore() throws Exception
    {
        assertNotNull( blockingCommandCallback.getCompletionSemaphore() );
    }


    @Test
    public void testGetCommandResult() throws Exception
    {
        blockingCommandCallback.completionSemaphore = completionSemaphore;

        blockingCommandCallback.getCommandResult();

        verify( completionSemaphore ).acquire();
    }


    @Test
    public void testOnResponse() throws Exception
    {
        blockingCommandCallback.completionSemaphore = completionSemaphore;

        blockingCommandCallback.onResponse( response, commandResult );

        verify( completionSemaphore ).release();
        verify( callback ).onResponse( response, commandResult );
    }
}
