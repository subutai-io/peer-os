package io.subutai.core.environment.impl.entity;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.RhP2pIp;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Mockito.mock;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentPeerImplTest
{

    private static final String PEER_ID = "123";
    private static final Integer VLAN = 123;

    EnvironmentPeerImpl environmentPeer;


    @Before
    public void setUp() throws Exception
    {
        environmentPeer = new EnvironmentPeerImpl( PEER_ID, VLAN );
    }


    @Test
    public void testGetPeerId() throws Exception
    {
        assertEquals( PEER_ID, environmentPeer.getPeerId() );
    }


    @Test
    public void testGetVlan() throws Exception
    {
        assertEquals( VLAN, environmentPeer.getVlan() );
    }


    @Test
    public void testRhP2pIps() throws Exception
    {
        environmentPeer.addRhP2pIps( Sets.<RhP2pIp>newHashSet( new RhP2PIpEntity( "ID", "10.10.10.1" ) ) );

        assertFalse( environmentPeer.getRhP2pIps().isEmpty() );
    }


    @Test
    public void testEnvironment() throws Exception
    {
        Environment environment = mock( Environment.class );

        environmentPeer.setEnvironment( environment );

        assertEquals( environment, environmentPeer.getEnvironment() );
    }
}
