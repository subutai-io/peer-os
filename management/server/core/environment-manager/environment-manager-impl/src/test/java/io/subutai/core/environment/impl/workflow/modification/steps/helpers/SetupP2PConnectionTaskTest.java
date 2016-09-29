package io.subutai.core.environment.impl.workflow.modification.steps.helpers;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.core.environment.impl.TestHelper;

import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class SetupP2PConnectionTaskTest
{
    SetupP2PConnectionTask task;


    Peer peer = TestHelper.PEER();
    @Mock
    P2PConfig p2PConfig;


    @Before
    public void setUp() throws Exception
    {
        task = new SetupP2PConnectionTask( peer, p2PConfig );
    }


    @Test
    public void testCall() throws Exception
    {
        task.call();

        verify( peer ).joinOrUpdateP2PSwarm( p2PConfig );
    }
}
