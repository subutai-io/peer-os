package org.safehaus.subutai.core.channel.impl;


import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.core.channel.api.token.ChannelTokenManager;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ChannelManagerImplTest
{
    private ChannelManagerImpl channelManager;

    @Mock
    DaoManager daoManager;
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    ChannelTokenManager channelTokenManager;

    @Before
    public void setUp() throws Exception
    {
        channelManager = new ChannelManagerImpl();
        channelManager.setDaoManager( daoManager );
        channelManager.setChannelTokenManager( channelTokenManager );
    }


    @Test
    public void testInit() throws Exception
    {
        when(daoManager.getEntityManagerFactory()).thenReturn( entityManagerFactory );


        channelManager.init();
    }


    @Test
    public void testDestroy() throws Exception
    {
        channelManager.destroy();
    }


    @Test
    public void testGetDaoManager() throws Exception
    {
        assertNotNull(channelManager.getDaoManager());
    }


    @Test
    public void testGetChannelTokenManager() throws Exception
    {
        assertNotNull( channelManager.getChannelTokenManager() );
    }


    @Test
    public void testGetIdentityManager() throws Exception
    {
        channelManager.getIdentityManager();
    }
}