package io.subutai.core.environment.impl.workflow.creation.steps;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;

import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class RegisterHostsStepTest
{
    RegisterHostsStep step;


    @Mock
    Topology topology;
    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    EnvironmentContainerImpl environmentContainer = TestHelper.ENV_CONTAINER();
    @Mock
    PeerUtil<Object> peerUtil;
    @Mock
    PeerUtil.PeerTaskResults<Object> peerTaskResults;
    @Mock
    PeerUtil.PeerTaskResult peerTaskResult;
    Peer peer = TestHelper.PEER();


    @Before
    public void setUp() throws Exception
    {
        doReturn( Sets.newHashSet( environmentContainer ) ).when( environment ).getContainerHosts();
        doReturn( true ).when( topology ).registerHosts();
        TestHelper.bind( environment, peer, peerUtil, peerTaskResults, peerTaskResult );

        step = spy( new RegisterHostsStep( topology, environment, TestHelper.TRACKER_OPERATION() ) );
        step.hostUtil = peerUtil;
    }


    @Test( expected = EnvironmentManagerException.class )
    public void testExecute() throws Exception
    {
        doReturn( true ).when( peerTaskResult ).hasSucceeded();

        step.execute();

        verify( step ).registerHosts( anySet() );

        doReturn( false ).when( peerTaskResult ).hasSucceeded();
        doReturn( true ).when( peerTaskResults ).hasFailures();

        step.execute();
    }
}

