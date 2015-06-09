package org.safehaus.subutai.core.channel.impl.token;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.channel.api.token.ChannelTokenManager;

import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ChannelTokenControllerTest
{
    private ChannelTokenController channelTokenController;

    @Mock
    ChannelTokenManager channelTokenManager;

    @Before
    public void setUp() throws Exception
    {
        channelTokenController = new ChannelTokenController( channelTokenManager );
    }


    @Test
    public void testRun() throws Exception
    {
        channelTokenController.run();

        verify(channelTokenManager).setTokenValidity();
    }
}