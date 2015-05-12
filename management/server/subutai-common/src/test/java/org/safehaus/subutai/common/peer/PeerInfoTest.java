package org.safehaus.subutai.common.peer;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class PeerInfoTest
{
    private PeerInfo peerInfo;


    @Mock
    PeerPolicy peerPolicy;
    @Mock
    PeerPolicy peerPolicy2;


    @Before
    public void setUp() throws Exception
    {
        when( peerPolicy.getRemotePeerId() ).thenReturn( new UUID( 50, 50 ) );
        when( peerPolicy2.getRemotePeerId() ).thenReturn( UUID.randomUUID() );

        Set<PeerPolicy> mySet = new HashSet<>();
        mySet.add( peerPolicy );
        mySet.add( peerPolicy2 );

        peerInfo = new PeerInfo();
        peerInfo.setId( UUID.randomUUID() );
        peerInfo.setGatewayIp( "testGateWayIp" );
        peerInfo.setIp( "testIp" );
        peerInfo.setKeyId( "testKeyId" );
        peerInfo.setLastUsedVlanId( 555 );
        peerInfo.setName( "testName" );
        peerInfo.setOwnerId( UUID.randomUUID() );
        peerInfo.setPeerPolicy( peerPolicy );
        peerInfo.setPeerPolicies( mySet );
        peerInfo.setPort( 8252 );
        peerInfo.setStatus( PeerStatus.APPROVED );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( peerInfo.getId() );
        assertNotNull( peerInfo.getName() );
        assertNotNull( peerInfo.getOwnerId() );
        assertNotNull( peerInfo.getIp() );
        assertNotNull( peerInfo.getGatewayIp() );
        assertNotNull( peerInfo.getStatus() );
        assertNotNull( peerInfo.getPort() );
        assertNotNull( peerInfo.getLastUsedVlanId() );
        assertNotNull( peerInfo.getKeyId() );
        assertNotNull( peerInfo.getPeerPolicies() );
        assertNotNull( peerInfo.getPeerPolicy( new UUID( 50, 50 ) ) );
        peerInfo.hashCode();
        peerInfo.equals( "test" );
        peerInfo.equals( peerInfo );
    }
}