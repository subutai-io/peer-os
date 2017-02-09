package io.subutai.core.registration.impl.entity.entity;


import java.util.HashSet;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.core.registration.api.ResourceHostRegistrationStatus;
import io.subutai.core.registration.impl.entity.RequestedHostImpl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class RequestedHostImplTest
{

    private RequestedHostImpl requestedHost;
    UUID uuid = UUID.randomUUID();


    @Before
    public void setUp() throws Exception
    {
        requestedHost =
                new RequestedHostImpl( uuid.toString(), "hostname", HostArchitecture.AMD64, "secret", "publicKey",
                        ResourceHostRegistrationStatus.REQUESTED, new HashSet<HostInterface>() );
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
        assertArrayEquals( Sets.newHashSet().toArray(), requestedHost.getInterfaces().toArray() );
    }


    @Test
    public void testSetInterfaces() throws Exception
    {
        HostInterfaces hostInterfaces = mock( HostInterfaces.class );
        HostInterfaceModel hostInterfaceModel = mock( HostInterfaceModel.class );
        doReturn( Sets.newHashSet( hostInterfaceModel ) ).when( hostInterfaces ).getAll();
        doReturn( "name" ).when( hostInterfaceModel ).getName();
        doReturn( "ip" ).when( hostInterfaceModel ).getIp();
        requestedHost.setInterfaces( hostInterfaces );
        assertEquals( 1, requestedHost.getInterfaces().size() );
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
    public void testGetStatus() throws Exception
    {
        assertEquals( ResourceHostRegistrationStatus.REQUESTED, requestedHost.getStatus() );
    }


    @Test
    public void testSetStatus() throws Exception
    {
        requestedHost.setStatus( ResourceHostRegistrationStatus.REJECTED );
        assertNotEquals( ResourceHostRegistrationStatus.REQUESTED, requestedHost.getStatus() );
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