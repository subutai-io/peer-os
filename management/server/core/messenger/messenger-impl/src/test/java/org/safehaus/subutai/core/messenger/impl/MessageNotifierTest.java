package org.safehaus.subutai.core.messenger.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageListener;
import org.slf4j.Logger;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
        Logger logger = mock( Logger.class );
        messageNotifier.LOG = logger;
        Exception exception = new RuntimeException();
        doThrow( exception ).when( listener ).onMessage( message );

        messageNotifier.run();

        verify( logger ).error( anyString(), eq( exception ) );
    }
}
