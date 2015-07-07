package io.subutai.core.env.impl.tasks;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.environment.NodeGroup;
import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.common.network.Gateway;
import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.ExceptionUtil;
import io.subutai.core.env.api.exception.EnvironmentCreationException;
import io.subutai.core.env.impl.EnvironmentManagerImpl;
import io.subutai.core.env.impl.TestUtil;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.ResultHolder;

import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CreateEnvironmentTaskTest
{
    @Mock
    LocalPeer localPeer;
    @Mock
    EnvironmentManagerImpl environmentManager;
    @Mock
    EnvironmentImpl environment;
    @Mock
    Topology topology;
    @Mock
    ResultHolder<EnvironmentCreationException> resultHolder;
    @Mock
    TrackerOperation operation;
    @Mock
    ExceptionUtil exceptionUtil;
    @Mock
    Semaphore semaphore;
    @Mock
    Peer peer;
    @Mock
    NodeGroup nodeGroup;
    @Mock
    Gateway gateway;
    @Mock
    ManagementHost managementHost;
    @Mock
    EnvironmentCreationException environmentCreationException;

    CreateEnvironmentTask task;
    Map<Peer, Set<NodeGroup>> placement;
    Map<Peer, Set<Gateway>> usedGateways;


    @Before
    public void setUp() throws Exception
    {
        task = new CreateEnvironmentTask( localPeer, environmentManager, environment, topology, resultHolder,
                operation );
        task.exceptionUtil = exceptionUtil;
        task.semaphore = semaphore;
        placement = Maps.newHashMap();
        placement.put( peer, Sets.newHashSet( nodeGroup ) );
        when( topology.getNodeGroupPlacement() ).thenReturn( placement );
        usedGateways = Maps.newHashMap();
        usedGateways.put( peer, Sets.newHashSet( gateway ) );
        when( environmentManager.getUsedGateways( anySet() ) ).thenReturn( usedGateways );
        when( environment.getSubnetCidr() ).thenReturn( TestUtil.SUBNET );
        when( gateway.getIp() ).thenReturn( TestUtil.GATEWAY_IP );
        when( environmentManager.findFreeVni( anySet() ) ).thenReturn( TestUtil.VNI );
        when( environment.getId() ).thenReturn( TestUtil.ENV_ID );
        when( localPeer.reserveVni( any( Vni.class ) ) ).thenReturn( TestUtil.VLAN );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( resultHolder.getResult() ).thenReturn( environmentCreationException );
    }


    @Test
    public void testWaitCompletion() throws Exception
    {
        task.waitCompletion();

        verify( semaphore ).acquire();
    }


    @Test
    public void testRun() throws Exception
    {

        task.run();

        verify( environmentCreationException ).getMessage();

        when( gateway.getIp() ).thenReturn( "127.0.0.1" );

        task.run();

        verify( environmentManager ).growEnvironment( TestUtil.ENV_ID, topology, false, false, operation );

        verify( semaphore, times( 2 ) ).release();
    }
}
