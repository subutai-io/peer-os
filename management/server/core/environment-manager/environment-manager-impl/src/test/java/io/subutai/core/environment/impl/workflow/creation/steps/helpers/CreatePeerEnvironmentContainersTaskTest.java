package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Node;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.impl.TestHelper;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;


@RunWith( MockitoJUnitRunner.class )
public class CreatePeerEnvironmentContainersTaskTest
{


    @Mock
    CreateEnvironmentContainersResponse response;


    CreatePeerEnvironmentContainersTask task;


    @Before
    public void setUp() throws Exception
    {
        Environment ENVIRONMENT = TestHelper.ENVIRONMENT();
        Node NODE = TestHelper.NODE();
        Peer PEER = TestHelper.PEER();
        LocalPeer LOCAL_PEER = TestHelper.LOCAL_PEER();

        doReturn( response ).when( PEER )
                            .createEnvironmentContainers( any( CreateEnvironmentContainersRequest.class ) );
        doReturn( Sets.newHashSet( TestHelper.MESSAGE ) ).when( response ).getMessages();

        task = new CreatePeerEnvironmentContainersTask( PEER, LOCAL_PEER, ENVIRONMENT,
                Lists.newArrayList( Common.LOCAL_HOST_IP ), Sets.newHashSet( NODE ), TestHelper.TRACKER_OPERATION() );
    }


    @Test
    public void testCall() throws Exception
    {
        CreateEnvironmentContainersResponse resp = task.call();

        assertEquals( response, resp );
    }
}
