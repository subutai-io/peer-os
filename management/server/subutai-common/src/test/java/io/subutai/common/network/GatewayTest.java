package io.subutai.common.network;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.network.Gateway;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class GatewayTest
{
    private Gateway gateway;


    @Before
    public void setUp() throws Exception
    {
        gateway = new Gateway( 5, "test" );
    }


    @Test
    public void testGetVlan() throws Exception
    {
        assertNotNull( gateway.getVlan() );
    }


    @Test
    public void testGetIp() throws Exception
    {
        assertNotNull( gateway.getIp() );
    }


    @Test
    public void testEquals() throws Exception
    {
        gateway.equals( "test" );
    }


    @Test
    public void testEquals2() throws Exception
    {
        gateway.equals( gateway );
    }


    @Test
    public void testHashCode() throws Exception
    {
        gateway.hashCode();
    }
}