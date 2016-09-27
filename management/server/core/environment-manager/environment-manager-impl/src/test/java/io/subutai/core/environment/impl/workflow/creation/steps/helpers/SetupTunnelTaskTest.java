package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.P2pIps;
import io.subutai.core.environment.impl.TestHelper;

import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class SetupTunnelTaskTest
{
    SetupTunnelTask task;

    Peer PEER = TestHelper.PEER();

    @Mock
    P2pIps p2pIps;


    @Before
    public void setUp() throws Exception
    {
        task = new SetupTunnelTask( PEER, TestHelper.ENV_ID, p2pIps );
    }


    @Test
    public void testCall() throws Exception
    {
        task.call();

        verify( PEER ).setupTunnels( p2pIps, TestHelper.ENVIRONMENT_ID );
    }
}
