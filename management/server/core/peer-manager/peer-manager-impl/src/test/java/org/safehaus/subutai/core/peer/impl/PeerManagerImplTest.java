package org.safehaus.subutai.core.peer.impl;


import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * Created by bahadyr on 9/25/14.
 */
@RunWith( MockitoJUnitRunner.class )
public class PeerManagerImplTest
{

    PeerManagerImpl peerManager;
    @Mock
    DataSource dataSource;


    @Before
    public void setUp() throws Exception
    {
        peerManager = new PeerManagerImpl( dataSource );
    }


    @Test
    public void test()
    {
    }
}
