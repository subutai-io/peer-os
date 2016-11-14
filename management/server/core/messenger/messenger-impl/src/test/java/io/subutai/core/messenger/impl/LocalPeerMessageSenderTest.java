package io.subutai.core.messenger.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class LocalPeerMessageSenderTest
{
    @Mock
    MessengerImpl messenger;
    @Mock
    MessengerDataService messengerDataService;
    @Mock
    Envelope envelope1;
    @Mock
    Envelope envelope2;

    LocalPeerMessageSender localPeerMessageSender;


    @Before
    public void setUp() throws Exception
    {
        localPeerMessageSender =
                new LocalPeerMessageSender( messenger, messengerDataService, Sets.newHashSet( envelope1, envelope2 ) );
    }


    @Test
    public void testCall() throws Exception
    {
        boolean result = localPeerMessageSender.call();

        assertTrue( result );
        verify( messenger, times( 2 ) ).notifyListeners( isA( Envelope.class ) );
        verify( messengerDataService, times( 2 ) ).markAsSent( isA( Envelope.class ) );
    }
}
