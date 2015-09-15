package io.subutai.core.env.impl.builder;


import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.env.impl.TestUtil;

import io.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.EnvironmentBuildException;
import io.subutai.core.env.impl.exception.NodeGroupBuildException;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentBuilderTest
{

    @Mock
    TemplateRegistry templateRegistry;
    @Mock
    PeerManager peerManager;
    @Mock
    ExceptionUtil exceptionUtil;
    @Mock
    EnvironmentImpl environment;
    @Mock
    Topology topology;
    @Mock
    Peer peer;
    @Mock
    NodeGroup nodeGroup;
    @Mock
    LocalPeer localPeer;
    @Mock
    PeerInfo peerInfo;
    @Mock
    ExecutorService executor;
    @Mock
    CompletionService<Set<NodeGroupBuildResult>> completer;
    @Mock
    Future<Set<NodeGroupBuildResult>> future;
    @Mock
    NodeGroupBuildResult nodeGroupBuildResult;
    @Mock
    EnvironmentContainerImpl environmentContainer;
    Map<Peer, Set<NodeGroup>> placement;
    @Mock
    NodeGroupBuildException nodeGroupBuildException;
    @Mock
    ExecutionException executionException;
    @Mock
    PeerException peerException;

    EnvironmentBuilder environmentBuilder;


    @Before
    public void setUp() throws Exception
    {
        environmentBuilder =
                Mockito.spy( new EnvironmentBuilder( templateRegistry, peerManager, TestUtil.DEFAULT_DOMAIN ) );
        environmentBuilder.exceptionUtil = exceptionUtil;
        placement = Maps.newHashMap();
        placement.put( peer, Sets.newHashSet( nodeGroup ) );
        when( topology.getNodeGroupPlacement() ).thenReturn( placement );
        when( environment.getSubnetCidr() ).thenReturn( TestUtil.SUBNET );
        when( nodeGroup.getNumberOfContainers() ).thenReturn( TestUtil.NUMBER_OF_CONTAINERS );
        when( localPeer.getId() ).thenReturn( TestUtil.LOCAL_PEER_ID );
        when( peer.getId() ).thenReturn( TestUtil.PEER_ID );
        when( peer.getPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getIp() ).thenReturn( TestUtil.IP );
        when( environmentBuilder.getExecutor( 1 ) ).thenReturn( executor );
        when( environmentBuilder.getCompletionService( executor ) ).thenReturn( completer );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( completer.take() ).thenReturn( future );
        when( future.get() ).thenReturn( Sets.newHashSet( nodeGroupBuildResult ) );
        when( environment.getPeers() ).thenReturn( Sets.newHashSet( peer ) );
        when( nodeGroupBuildResult.getContainers() ).thenReturn( Sets.newHashSet( environmentContainer ) );
    }

    @Ignore
    @Test
    public void testBuild() throws Exception
    {
        environmentBuilder.build( environment, topology );

        verify( executor ).shutdown();

        when( nodeGroupBuildResult.getException() ).thenReturn( nodeGroupBuildException );

        try
        {
            environmentBuilder.build( environment, topology );
            fail( "Expected EnvironmentBuildException" );
        }
        catch ( EnvironmentBuildException e )
        {
        }

        when( nodeGroupBuildResult.getException() ).thenReturn( null );

        doThrow( executionException ).when( future ).get();

        try
        {
            environmentBuilder.build( environment, topology );
            fail( "Expected EnvironmentBuildException" );
        }
        catch ( EnvironmentBuildException e )
        {
        }

        reset( future );
        when( future.get() ).thenReturn( Sets.newHashSet( nodeGroupBuildResult ) );


        when( nodeGroup.getNumberOfContainers() ).thenReturn( Integer.MAX_VALUE );

        try
        {
            environmentBuilder.build( environment, topology );
            fail( "Expected EnvironmentBuildException" );
        }
        catch ( EnvironmentBuildException e )
        {
        }

        when( nodeGroup.getNumberOfContainers() ).thenReturn( TestUtil.NUMBER_OF_CONTAINERS );

        doThrow( peerException ).when( localPeer ).setupTunnels( anyMap(), any( String.class ) );

        try
        {
            environmentBuilder.build( environment, topology );
            fail( "Expected EnvironmentBuildException" );
        }
        catch ( EnvironmentBuildException e )
        {
        }
    }
}
