package io.subutai.core.env.impl.builder;


import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.environment.CreateContainerGroupRequest;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.host.Interface;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.HostInfoModel;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.PlacementStrategy;
import io.subutai.common.protocol.Template;
import io.subutai.core.env.impl.TestUtil;

import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.NodeGroupBuildException;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;

import com.google.common.collect.Sets;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class NodeGroupBuilderTest
{
    @Mock
    EnvironmentImpl environment;
    @Mock
    PeerManager peerManager;
    @Mock
    TemplateRegistry templateRegistry;
    @Mock
    Peer peer;
    @Mock
    Peer peer2;
    @Mock
    NodeGroup nodeGroup;
    @Mock
    Template template;
    @Mock
    PeerException peerException;
    @Mock
    Vni vni;
    @Mock
    Gateway gateway;
    @Mock
    LocalPeer localPeer;
    @Mock
    ManagementHost managementHost;
    @Mock
    HostInfoModel model;
    @Mock
    Interface netInterface;
    @Mock
    PeerInfo peerInfo;

    NodeGroupBuilder nodeGroupBuilder;


    @Before
    public void setUp() throws Exception
    {
        nodeGroupBuilder =
                new NodeGroupBuilder( environment, templateRegistry, peerManager, peer, Sets.newHashSet( nodeGroup ),
                        Sets.newHashSet( peer, peer2 ), TestUtil.DEFAULT_DOMAIN, 0 );
        when( peer.getId() ).thenReturn( TestUtil.PEER_ID );
        when( peer2.getId() ).thenReturn( UUID.randomUUID().toString() );
        when( peer.getPeerInfo() ).thenReturn( peerInfo );
        when( peer2.getPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getIp() ).thenReturn( TestUtil.IP );
        when( templateRegistry.getTemplate( TestUtil.TEMPLATE_NAME ) ).thenReturn( template );
        when( template.getRemoteClone( TestUtil.PEER_ID ) ).thenReturn( template );
        when( environment.getSubnetCidr() ).thenReturn( TestUtil.SUBNET );
        when( environment.getId() ).thenReturn( TestUtil.ENV_ID );
        when( environment.getVni() ).thenReturn( TestUtil.VNI );
        when( peer.getReservedVnis() ).thenReturn( Sets.newHashSet( vni ) );
        when( peer2.getReservedVnis() ).thenReturn( Sets.<Vni>newHashSet() );
        when( vni.getEnvironmentId() ).thenReturn( TestUtil.ENV_ID );
        when( peer.getGateways() ).thenReturn( Sets.newHashSet( gateway ) );
        when( peer2.getGateways() ).thenReturn( Sets.<Gateway>newHashSet() );
        when( gateway.getIp() ).thenReturn( TestUtil.GATEWAY_IP );
        when( gateway.getVlan() ).thenReturn( TestUtil.VLAN );
        when( vni.getVlan() ).thenReturn( TestUtil.VLAN );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( managementHost.getIpByInterfaceName( anyString() ) ).thenReturn( TestUtil.IP );
        when( nodeGroup.getTemplateName() ).thenReturn( TestUtil.TEMPLATE_NAME );
        when( localPeer.getId() ).thenReturn( TestUtil.LOCAL_PEER_ID );
        when( localPeer.getOwnerId() ).thenReturn( TestUtil.LOCAL_PEER_ID );
        when( nodeGroup.getNumberOfContainers() ).thenReturn( 1 );
        when( nodeGroup.getName() ).thenReturn( TestUtil.NODE_GROUP_NAME );
        when( nodeGroup.getContainerPlacementStrategy() ).thenReturn( new PlacementStrategy( "ROUND_ROBIN" ) );
        when( peer.createContainerGroup( any( CreateContainerGroupRequest.class ) ) )
                .thenReturn( Sets.newHashSet( model ) );
        when( peer2.createContainerGroup( any( CreateContainerGroupRequest.class ) ) )
                .thenReturn( Sets.newHashSet( model ) );
        when( model.getInterfaces() ).thenReturn( Sets.newHashSet( netInterface ) );
        when( netInterface.getIp() ).thenReturn( TestUtil.IP );
        when( model.getId() ).thenReturn( TestUtil.CONTAINER_ID );
    }


    @Test
    public void testFetchRequiredTemplates() throws Exception
    {
        nodeGroupBuilder.fetchRequiredTemplates( TestUtil.PEER_ID, TestUtil.TEMPLATE_NAME );

        when( templateRegistry.getTemplate( anyString() ) ).thenReturn( null );

        try
        {
            nodeGroupBuilder.fetchRequiredTemplates( TestUtil.PEER_ID, TestUtil.TEMPLATE_NAME );
            fail( "Expected NodeGroupBuildException" );
        }
        catch ( NodeGroupBuildException e )
        {
        }
    }


    @Test
    public void testCall() throws Exception
    {
        nodeGroupBuilder.call();
    }


    @Test( expected = NodeGroupBuildException.class )
    public void testCallWithException1() throws Exception
    {
        doThrow( peerException ).when( peer ).getReservedVnis();

        nodeGroupBuilder.call();
    }


    @Test( expected = NodeGroupBuildException.class )
    public void testCallWithException2() throws Exception
    {
        doThrow( peerException ).when( peer ).getGateways();

        nodeGroupBuilder.call();
    }


    @Test( expected = NodeGroupBuildException.class )
    public void testCallWithException3() throws Exception
    {
        when( vni.getVlan() ).thenReturn( -1 );

        nodeGroupBuilder.call();
    }


    @Test( expected = NodeGroupBuildException.class )
    public void testCallWithException4() throws Exception
    {
        when( vni.getEnvironmentId() ).thenReturn( TestUtil.ENV_ID);
        doThrow( peerException ).when( peer ).reserveVni( any( Vni.class ) );

        nodeGroupBuilder.call();
    }


    @Test
    public void testCallWithException5() throws Exception
    {
        when( nodeGroup.getNumberOfContainers() ).thenReturn( 2 );

        Set<NodeGroupBuildResult> resultSet = nodeGroupBuilder.call();

        assertFalse( resultSet.isEmpty() );
        assertNotNull( resultSet.iterator().next().getException() );
    }


    @Test
    @Ignore
    public void testCallWithException6() throws Exception
    {
        when( peer2.getPeerInfo() ).thenReturn( null );

        Set<NodeGroupBuildResult> resultSet = nodeGroupBuilder.call();

        assertFalse( resultSet.isEmpty() );
        assertNotNull( resultSet.iterator().next().getException() );
    }
}
