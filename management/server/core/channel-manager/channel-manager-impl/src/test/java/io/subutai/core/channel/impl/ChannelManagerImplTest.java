package io.subutai.core.channel.impl;


import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.dao.DaoManager;

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

    @Before
    public void setUp() throws Exception
    {
        channelManager = new ChannelManagerImpl();
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
    public void testGetIdentityManager() throws Exception
    {
        channelManager.getIdentityManager();
    }
}