package io.subutai.common.network;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class VniTest
{
    private Vni vni;


    @Before
    public void setUp() throws Exception
    {
        vni = new Vni( 555, 555, UUID.randomUUID().toString() );
        vni = new Vni( 555, UUID.randomUUID().toString() );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( vni.getVlan() );
        assertNotNull( vni.getEnvironmentId() );
        assertNotNull( vni.getVni() );
        vni.toString();
        vni.hashCode();
        vni.equals( "test" );
        vni.equals( vni );
    }
}