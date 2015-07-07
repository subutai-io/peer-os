package org.safehaus.subutai.core.peer.impl;


import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.peer.PeerPolicy;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.key.api.KeyManager;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.impl.command.CommandResponseListener;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;
import org.safehaus.subutai.core.peer.impl.entity.ManagementHostEntity;
import org.safehaus.subutai.core.peer.impl.request.MessageRequestListener;
import org.safehaus.subutai.core.peer.impl.request.MessageResponseListener;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.ssl.manager.api.SubutaiSslContextFactory;
import org.safehaus.subutai.core.strategy.api.StrategyManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
    private static final UUID PEER_ID = UUID.randomUUID();
    @Mock
    PeerDAO peerDAO;
    @Mock
    QuotaManager quotaManager;
    @Mock
    Monitor monitor;
    @Mock
    TemplateRegistry templateRegistry;
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
    KeyManager keyManager;
    @Mock
    IdentityManager identityManager;
    @Mock
    SubutaiSslContextFactory sslContextFactory;
    @Mock
    ManagementHostEntity managementHost;
    @Mock
    Set<RequestListener> requestListeners;

    PeerManagerImpl peerManager;


    @Before
    public void setUp() throws Exception
    {
        peerManager = spy( new PeerManagerImpl( messenger, localPeer, daoManager , messageResponseListener) );


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
    public void testRegister() throws Exception
    {
        peerManager.register( peerInfo );

        verify( peerDAO ).saveInfo( anyString(), anyString(), eq( peerInfo ) );
    }


    @Test
    public void testUnregister() throws Exception
    {

        PeerPolicy peerPolicy = mock( PeerPolicy.class );
        when( peerInfo.getPeerPolicy( PEER_ID ) ).thenReturn( peerPolicy );
        when( peerInfo.getPeerPolicies() ).thenReturn( Sets.newHashSet( peerPolicy ) );
        when( peerManager.getPeerInfo( PEER_ID ) ).thenReturn( peerInfo );


        peerManager.unregister( PEER_ID.toString() );

        verify( peerDAO ).saveInfo( anyString(), anyString(), anyObject() );

        verify( peerDAO ).deleteInfo( anyString(), anyString() );
    }


    @Test
    public void testUpdate() throws Exception
    {
        peerManager.update( peerInfo );

        verify( peerDAO ).saveInfo( anyString(), anyString(), anyObject() );

        when( peerInfo.getId() ).thenReturn( UUID.randomUUID() );

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

        peerManager.getPeerInfo( id );

        verify( peerDAO ).getInfo( PeerManagerImpl.SOURCE_REMOTE_PEER, id.toString(), PeerInfo.class );
    }


    @Test
    public void testGetPeer() throws Exception
    {
        assertEquals( localPeer, peerManager.getPeer( PEER_ID ) );

        doReturn( peerInfo ).when( peerManager ).getPeerInfo( any( UUID.class ) );

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


    @Test
    public void testAddRequestListener() throws Exception
    {
        peerManager.addRequestListener( requestListener );

        verify( localPeer ).addRequestListener( requestListener );
    }


    @Test
    public void testRemoveListener() throws Exception
    {
        peerManager.removeRequestListener( requestListener );

        verify( localPeer ).removeRequestListener( requestListener );
    }
}
