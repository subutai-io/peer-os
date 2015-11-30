package io.subutai.core.registration.impl.entity.entity;


import java.util.HashSet;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInterface;
import io.subutai.core.registration.api.RegistrationStatus;
import io.subutai.core.registration.impl.entity.RequestedHostImpl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;


/**
 * Created by talas on 8/26/15.
 */
public class RequestedHostImplTest
{

    private RequestedHostImpl requestedHost;
    UUID uuid = UUID.randomUUID();


    @Before
    public void setUp() throws Exception
    {
        requestedHost =
                new RequestedHostImpl( uuid.toString(), "hostname", HostArchitecture.AMD64, "secret", "publicKey",
                        "restHook", RegistrationStatus.REQUESTED, new HashSet<HostInterface>() );
    }


    @Test
    public void testGetId() throws Exception
    {
        assertEquals( uuid.toString(), requestedHost.getId() );
    }


    @Test
    public void testGetHostname() throws Exception
    {
        assertEquals( "hostname", requestedHost.getHostname() );
    }


    @Test
    public void testGetInterfaces() throws Exception
    {
        assertArrayEquals( Sets.newHashSet().toArray(), requestedHost.getNetHostInterfaces().toArray() );
    }


    @Test
    public void testSetInterfaces() throws Exception
    {
        requestedHost.setNetHostInterfaces( Sets.newHashSet( mock( HostInterface.class ) ) );
        assertEquals( 1, requestedHost.getNetHostInterfaces().size() );
    }


    @Test
    public void testGetArch() throws Exception
    {
        assertEquals( HostArchitecture.AMD64, requestedHost.getArch() );
    }


    @Test
    public void testGetPublicKey() throws Exception
    {
        assertEquals( "publicKey", requestedHost.getPublicKey() );
    }


    @Test
    public void testGetRestHook() throws Exception
    {
        assertEquals( "restHook", requestedHost.getRestHook() );
    }


    @Test
    public void testGetStatus() throws Exception
    {
        assertEquals( RegistrationStatus.REQUESTED, requestedHost.getStatus() );
    }


    @Test
    public void testSetStatus() throws Exception
    {
        requestedHost.setStatus( RegistrationStatus.REJECTED );
        assertNotEquals( RegistrationStatus.REQUESTED, requestedHost.getStatus() );
    }


    @Test
    public void testGetSecret() throws Exception
    {
        requestedHost.setSecret( "secret" );
        assertEquals( "secret", requestedHost.getSecret() );
    }


    @Test
    public void testSetSecret() throws Exception
    {
        requestedHost.setSecret( "secret1" );
        assertNotEquals( "secret", requestedHost.getSecret() );
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