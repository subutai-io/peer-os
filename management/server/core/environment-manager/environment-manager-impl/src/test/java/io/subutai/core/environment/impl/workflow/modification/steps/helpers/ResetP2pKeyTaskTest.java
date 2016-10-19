package io.subutai.core.environment.impl.workflow.modification.steps.helpers;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.TestHelper;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ResetP2pKeyTaskTest
{
    ResetP2pKeyTask task;
    Peer peer = TestHelper.PEER();
    TrackerOperation trackerOperation = TestHelper.TRACKER_OPERATION();
    @Mock
    P2PCredentials p2PCredentials;


    @Before
    public void setUp() throws Exception
    {
        task = new ResetP2pKeyTask( peer, p2PCredentials, trackerOperation );
    }


    @Test
    public void testCall() throws Exception
    {
        doReturn( RegistrationStatus.APPROVED ).when( peer ).getStatus();

        task.call();

        verify( peer ).resetSwarmSecretKey( p2PCredentials );

        doReturn( RegistrationStatus.CANCELLED ).when( peer ).getStatus();

        task.call();

        verify( trackerOperation ).addLog( anyString() );
    }
}
