package io.subutai.core.messenger.impl;


import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.MessageListener;
import io.subutai.core.messenger.impl.MessageNotifier;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class MessageNotifierTest
{
    @Mock
    MessageListener listener;
    @Mock
    Message message;

    MessageNotifier messageNotifier;


    @Before
    public void setUp() throws Exception
    {
        messageNotifier = new MessageNotifier( listener, message );
    }


    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new MessageNotifier( null, message );
            fail( "Exception expected on null message listener" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {
            new MessageNotifier( listener, null );
            fail( "Exception expected on null message" );
        }
        catch ( NullPointerException e )
        {
        }
    }


    @Test
    public void testRun() throws Exception
    {

        messageNotifier.run();

        verify( listener ).onMessage( message );
    }


    @Test
    public void testRunWithException() throws Exception
    {
        Exception exception = mock( RuntimeException.class );
        doThrow( exception ).when( listener ).onMessage( message );

        messageNotifier.run();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
