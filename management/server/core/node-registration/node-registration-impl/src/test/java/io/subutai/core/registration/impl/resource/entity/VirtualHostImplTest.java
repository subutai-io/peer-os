package io.subutai.core.registration.impl.resource.entity;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.subutai.common.host.HostArchitecture;

import static org.junit.Assert.assertEquals;


/**
 * Created by talas on 8/26/15.
 */
public class VirtualHostImplTest
{

    VirtualHostImpl virtualHost;

    UUID uuid;


    @Before
    public void setUp() throws Exception
    {
        uuid = UUID.randomUUID();
        virtualHost = new VirtualHostImpl( uuid.toString(), "hostname", HostArchitecture.AMD64 );
    }


    @Test
    public void testGetId() throws Exception
    {
        assertEquals( uuid.toString(), virtualHost.getId() );
    }


    @Test
    public void testGetHostname() throws Exception
    {
        assertEquals( "hostname", virtualHost.getHostname() );
    }


    @Test
    public void testGetInterfaces() throws Exception
    {
        assertEquals( 0, virtualHost.getInterfaces().size() );
    }


    @Test
    public void testGetArch() throws Exception
    {
        assertEquals( HostArchitecture.AMD64, virtualHost.getArch() );
    }


    @Test
    public void testEquals() throws Exception
    {

    }


    @Test
    public void testHashCode() throws Exception
    {

    }


    @Test
    public void testToString() throws Exception
    {

    }
}