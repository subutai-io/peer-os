package org.safehaus.subutai.core.peer.impl;


import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.messenger.api.Messenger;



@Ignore
@RunWith(MockitoJUnitRunner.class)
public class PeerManagerImplTest
{

    PeerManagerImpl peerManager;

    @Mock
    Messenger messenger;


    @Before
    public void setUp() throws Exception
    {
        peerManager = new PeerManagerImpl( messenger );
    }


    @Test
    public void test()
    {
    }
}
