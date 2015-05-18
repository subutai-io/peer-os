package org.safehaus.subutai.core.peer.rest;


import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.peer.PeerPolicy;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.ssl.manager.api.SubutaiSslContextFactory;

import static junit.framework.TestCase.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RestServiceImplTest
{
    private static final UUID PEER_ID = UUID.randomUUID();
    private static final String JSON = "json";
    @Mock
    SubutaiSslContextFactory sslContextFactory;
    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    PeerInfo peerInfo;
    @Mock
    PeerPolicy peerPolicy;
    @Mock
    JsonUtil jsonUtil;

    RestServiceImpl restService;


    @Before
    public void setUp() throws Exception
    {
        restService = new RestServiceImpl( peerManager, sslContextFactory );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getId() ).thenReturn( PEER_ID );
        when( localPeer.getPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getPeerPolicy( any( UUID.class ) ) ).thenReturn( peerPolicy );
        restService.jsonUtil = jsonUtil;
    }


    @Test
    public void testGetSelfPeerInfo() throws Exception
    {
        restService.getSelfPeerInfo();

        verify( peerManager ).getLocalPeerInfo();
    }


    @Test
    public void testGetId() throws Exception
    {
        restService.getId();

        verify( localPeer ).getId();
    }


    @Test
    public void testGetRegisteredPeers() throws Exception
    {
        restService.getRegisteredPeers();

        verify( peerManager ).getPeerInfos();
    }


    @Test
    public void testGetPeerPolicy() throws Exception
    {

        when( jsonUtil.to( anyObject() ) ).thenReturn( JSON );

        Response response = restService.getPeerPolicy( PEER_ID.toString() );

        verify( jsonUtil ).to( peerPolicy );


        reset( jsonUtil );

        when( peerInfo.getPeerPolicy( PEER_ID ) ).thenReturn( null );

        response = restService.getPeerPolicy( PEER_ID.toString() );

        assertNull( response.getEntity() );

    }
}
