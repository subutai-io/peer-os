package org.safehaus.subutai.core.env.impl.tasks;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.core.env.impl.TestUtil;

import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class PeerEnvironmentDestructionTaskTest
{
    @Mock
    Peer peer;

    PeerEnvironmentDestructionTask task;


    @Before
    public void setUp() throws Exception
    {
        task = new PeerEnvironmentDestructionTask( peer, TestUtil.ENV_ID );
    }


    @Test
    public void testCall() throws Exception
    {
        task.call();

        verify( peer ).destroyEnvironmentContainers( TestUtil.ENV_ID );
    }
}
