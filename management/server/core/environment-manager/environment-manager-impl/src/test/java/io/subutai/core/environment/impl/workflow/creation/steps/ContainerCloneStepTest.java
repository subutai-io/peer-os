package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneResponse;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.quota.ContainerSize;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ContainerCloneStepTest
{
    ContainerCloneStep step;
    @Mock
    Topology topology;
    @Mock
    PeerManager peerManager;

    LocalEnvironment ENVIRONMENT = TestHelper.ENVIRONMENT();
    LocalPeer LOCAL_PEER = TestHelper.LOCAL_PEER();
    Peer PEER = TestHelper.PEER();

    @Mock
    PeerUtil<CreateEnvironmentContainersResponse> PEER_UTIL;
    @Mock
    PeerUtil.PeerTaskResults<Object> peerTaskResults;
    @Mock
    PeerUtil.PeerTaskResult peerTaskResult;
    EnvironmentContainerImpl environmentContainer = TestHelper.ENV_CONTAINER();
    Node NODE = TestHelper.NODE();
    @Mock
    CreateEnvironmentContainersResponse response;
    @Mock
    CloneResponse cloneResponse;
    @Mock
    IdentityManager identityManager;


    @Before
    public void setUp() throws Exception
    {

        doReturn( LOCAL_PEER ).when( peerManager ).getLocalPeer();

        step = spy( new ContainerCloneStep( Common.DEFAULT_DOMAIN_NAME, topology, ENVIRONMENT, peerManager,
                identityManager, TestHelper.TRACKER_OPERATION() ) );
        step.cloneUtil = PEER_UTIL;

        TestHelper.bind( ENVIRONMENT, PEER, PEER_UTIL, peerTaskResults, peerTaskResult );

        doReturn( Sets.newHashSet( environmentContainer ) ).when( ENVIRONMENT ).getContainerHosts();
        Map<String, Set<Node>> placement = Maps.newHashMap();
        placement.put( TestHelper.RH_ID, Sets.newHashSet( NODE ) );

        doReturn( placement ).when( topology ).getNodeGroupPlacement();

        doReturn( PEER ).when( peerManager ).getPeer( TestHelper.PEER_ID );
    }


    @Test( expected = EnvironmentCreationException.class )
    public void testExecute() throws Exception
    {
        doReturn( true ).when( step ).processResponse( any( CreateEnvironmentContainersResponse.class ), anyString() );

        step.execute();

        verify( PEER_UTIL ).addPeerTask( any( PeerUtil.PeerTask.class ) );

        doReturn( false ).when( step ).processResponse( any( CreateEnvironmentContainersResponse.class ), anyString() );

        step.execute();
    }


    @Test
    public void testProcessResponse() throws Exception
    {
        doReturn( Sets.newHashSet( cloneResponse ) ).when( response ).getResponses();
        doReturn( environmentContainer ).when( step ).buildContainerEntity( TestHelper.PEER_ID, cloneResponse );

        step.processResponse( response, TestHelper.PEER_ID );

        verify( step ).buildContainerEntity( TestHelper.PEER_ID, cloneResponse );
        verify( ENVIRONMENT ).addContainers( anySet() );
    }


    @Test
    public void testBuildContainerEntity() throws Exception
    {
        doReturn( TestHelper.TEMPLATE_ID ).when( cloneResponse ).getTemplateId();
        doReturn( TestHelper.RH_ID ).when( cloneResponse ).getResourceHostId();
        doReturn( TestHelper.CONTAINER_QUOTA ).when( cloneResponse ).getContainerQuota();

        assertNotNull( step.buildContainerEntity( TestHelper.PEER_ID, cloneResponse ) );
    }
}
