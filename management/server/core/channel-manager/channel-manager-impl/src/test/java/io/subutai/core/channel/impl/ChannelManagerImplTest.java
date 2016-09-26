package io.subutai.core.channel.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.security.api.SecurityManager;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class ChannelManagerImplTest
{
    private ChannelManagerImpl channelManager;

    @Mock
    SecurityManager securityManager;
    @Mock
    IdentityManager identityManager;


    @Before
    public void setUp() throws Exception
    {
        channelManager = new ChannelManagerImpl();
        channelManager.setIdentityManager( identityManager );
        channelManager.setSecurityManager( securityManager );
    }


    @Test
    public void testGetIdentityManager() throws Exception
    {
        assertEquals( identityManager, channelManager.getIdentityManager() );
    }


    @Test
    public void testGetSecurityManager() throws Exception
    {
        assertEquals( securityManager, channelManager.getSecurityManager() );
    }
}