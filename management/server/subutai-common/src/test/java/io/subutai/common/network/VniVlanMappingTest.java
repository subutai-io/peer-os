package io.subutai.common.network;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.network.VniVlanMapping;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class VniVlanMappingTest
{
    private VniVlanMapping vniVlanMapping;


    @Before
    public void setUp() throws Exception
    {
        vniVlanMapping = new VniVlanMapping( 555, 555, 555, UUID.randomUUID().toString() );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( vniVlanMapping.getVni() );
        assertNotNull( vniVlanMapping.getEnvironmentId() );
        assertNotNull( vniVlanMapping.getTunnelId() );
        assertNotNull( vniVlanMapping.getVlan() );
        vniVlanMapping.hashCode();
        vniVlanMapping.toString();
        vniVlanMapping.equals( "test" );
        vniVlanMapping.equals( vniVlanMapping );
        vniVlanMapping.equals( 555 );
    }
}