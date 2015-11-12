package io.subutai.core.peer.impl;


import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.RequestListener;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.http.manager.api.HttpContextManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.localpeer.impl.LocalPeerImpl;
import io.subutai.core.localpeer.impl.entity.ManagementHostEntity;
import io.subutai.core.localpeer.impl.request.MessageRequestListener;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.peer.impl.command.CommandResponseListener;
import io.subutai.core.peer.impl.dao.PeerDAO;
import io.subutai.core.peer.impl.request.MessageResponseListener;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.strategy.api.StrategyManager;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class PeerManagerImplTest
{
    private static final String PEER_ID_FILE = "./id";
    private static final String PEER_ID = UUID.randomUUID().toString();
    @Mock
    PeerDAO peerDAO;
    @Mock
    QuotaManager quotaManager;
    @Mock
    Monitor monitor;

    @Mock
    CommandExecutor commandExecutor;
    @Mock
    LocalPeerImpl localPeer;
    @Mock
    StrategyManager strategyManager;
    @Mock
    PeerInfo peerInfo;
    @Mock
    Messenger messenger;
    @Mock
    CommandResponseListener commandResponseListener;
    @Mock
    RequestListener requestListener;
    @Mock
    MessageResponseListener messageResponseListener;
    @Mock
    MessageRequestListener messageRequestListener;
    @Mock
    HostRegistry hostRegistry;
    @Mock
    DaoManager daoManager;
    @Mock
    IdentityManager identityManager;
    @Mock
    SecurityManager securityManager;
    @Mock
    ManagementHostEntity managementHost;
    @Mock
    Set<RequestListener> requestListeners;
    @Mock
    HttpContextManager httpContextManager;

    PeerManagerImpl peerManager;


    @Before
    public void setUp() throws Exception
    {
        peerManager =
                spy( new PeerManagerImpl( messenger, localPeer, httpContextManager, daoManager, messageResponseListener,
                        securityManager, null ) );


        peerManager.commandResponseListener = commandResponseListener;
        peerManager.localPeer = localPeer;
        peerManager.peerDAO = peerDAO;

        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        //        doNothing().when( peerManager ).initPeerInfo();
        InetAddress inetAddress = mock( InetAddress.class );
        when( peerInfo.getId() ).thenReturn( PEER_ID );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( localPeer.getPeerInfo() ).thenReturn( peerInfo );
        when( localPeer.getId() ).thenReturn( PEER_ID );
    }


    @Test
    public void testInit() throws Exception
    {
        peerManager.init();

        verify( localPeer, atLeastOnce() ).addRequestListener( any( RequestListener.class ) );
    }


    @Test
    public void testDestroy() throws Exception
    {
        peerManager.destroy();

        verify( commandResponseListener ).dispose();
    }


    @Test
    public void testUnregister() throws Exception
    {

        PeerPolicy peerPolicy = mock( PeerPolicy.class );
        when( peerInfo.getPeerPolicy( PEER_ID ) ).thenReturn( peerPolicy );
        when( peerInfo.getPeerPolicies() ).thenReturn( Sets.newHashSet( peerPolicy ) );
        when( peerManager.getPeerInfo( PEER_ID ) ).thenReturn( peerInfo );


        //peerManager.unregister( PEER_ID.toString() );

        //verify( peerDAO ).saveInfo( anyString(), anyString(), anyObject() );

        //verify( peerDAO ).deleteInfo( anyString(), anyString() );
    }


    @Test
    public void testUpdate() throws Exception
    {
        peerManager.update( peerInfo );

        verify( peerDAO ).saveInfo( anyString(), anyString(), anyObject() );

        when( peerInfo.getId() ).thenReturn( UUID.randomUUID().toString() );

        peerManager.update( peerInfo );

        verify( peerDAO, times( 2 ) ).saveInfo( anyString(), anyString(), anyObject() );
    }


    @Test
    public void testGetPeerInfos() throws Exception
    {
        when( peerDAO.getInfo( anyString(), eq( PeerInfo.class ) ) ).thenReturn( Lists.newArrayList( peerInfo ) );

        List<PeerInfo> infos = peerManager.getPeerInfos();

        assertTrue( infos.contains( peerInfo ) );
    }


    @Test
    public void testGetPeers() throws Exception
    {
        List<Peer> peers = peerManager.getPeers();

        assertTrue( peers.contains( localPeer ) );
    }


    @Test
    public void testGetPeerInfo() throws Exception
    {
        peerManager.getPeerInfo( PEER_ID );

        verify( peerDAO ).getInfo( PeerManagerImpl.SOURCE_LOCAL_PEER, PEER_ID.toString(), PeerInfo.class );

        UUID id = UUID.randomUUID();

        peerManager.getPeerInfo( id.toString() );

        verify( peerDAO ).getInfo( PeerManagerImpl.SOURCE_REMOTE_PEER, id.toString(), PeerInfo.class );
    }


    @Test
    public void testGetPeer() throws Exception
    {
        assertEquals( localPeer, peerManager.getPeer( PEER_ID ) );

        doReturn( peerInfo ).when( peerManager ).getPeerInfo( any( String.class ) );

        assertFalse( localPeer.equals( peerManager.getPeer( UUID.randomUUID().toString() ) ) );
    }


    @Test
    public void testGetLocalPeer() throws Exception
    {
        assertEquals( localPeer, peerManager.getLocalPeer() );
    }


    @Test
    public void testGetLocalPeerInfo() throws Exception
    {
        assertEquals( peerInfo, peerManager.getLocalPeerInfo() );
    }
}
