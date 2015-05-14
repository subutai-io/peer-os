package org.safehaus.subutai.core.hostregistry.impl;


import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostDisconnectedException;
import org.safehaus.subutai.core.hostregistry.api.HostListener;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;

import com.google.common.cache.Cache;
import com.google.common.collect.Sets;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class HostRegistryImplTest
{

    private static final UUID HOST_ID = UUID.randomUUID();
    private static final String HOST_HOSTNAME = "host";
    private static final UUID CONTAINER_ID = UUID.randomUUID();
    private static final String CONTAINER_HOSTNAME = "container";
    private static final String DUMMY_HOSTNAME = "dummy";
    private static final int HOST_EXPIRATION = 30;

    @Mock
    Set<HostListener> hostListeners;

    @Mock
    ExecutorService notifier;

    @Mock
    HeartBeatListener heartBeatListener;

    @Mock
    Cache<UUID, ResourceHostInfo> hosts;

    @Mock
    ConcurrentMap map;

    @Mock
    ResourceHostInfo resourceHostInfo;

    @Mock
    ContainerHostInfo containerHostInfo;

    @Mock
    HostListener hostListener;

    HostRegistryImpl registry;


    @Before
    public void setUp() throws Exception
    {
        registry = new HostRegistryImpl( HOST_EXPIRATION );
        registry.hostListeners = hostListeners;
        registry.notifier = notifier;
        registry.hosts = hosts;
        when( hosts.asMap() ).thenReturn( map );
        when( map.values() ).thenReturn( Sets.newHashSet( resourceHostInfo ) );
        when( resourceHostInfo.getContainers() ).thenReturn( Sets.newHashSet( containerHostInfo ) );
        when( resourceHostInfo.getId() ).thenReturn( HOST_ID );
        when( resourceHostInfo.getHostname() ).thenReturn( HOST_HOSTNAME );
        when( containerHostInfo.getId() ).thenReturn( CONTAINER_ID );
        when( containerHostInfo.getHostname() ).thenReturn( CONTAINER_HOSTNAME );
        Iterator<HostListener> hostListenerIterator = mock( Iterator.class );
        when( hostListeners.iterator() ).thenReturn( hostListenerIterator );
        when( hostListenerIterator.hasNext() ).thenReturn( true ).thenReturn( false );
        when( hostListenerIterator.next() ).thenReturn( hostListener );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testConstructor() throws Exception
    {
        new HostRegistryImpl( 0 );
    }


    @Test
    public void testGetContainerInfoById() throws Exception
    {
        ContainerHostInfo existingHost = registry.getContainerHostInfoById( CONTAINER_ID );

        assertNotNull( existingHost );

        try
        {
            registry.getContainerHostInfoById( UUID.randomUUID() );
            fail( "Expected HostDisconnectedException" );
        }
        catch ( HostDisconnectedException e )
        {
        }
    }


    @Test
    public void testGetHostInfoById() throws Exception
    {

        assertNotNull( registry.getHostInfoById( CONTAINER_ID ) );
        assertNotNull( registry.getHostInfoById( HOST_ID ) );

        try
        {
            registry.getHostInfoById( UUID.randomUUID() );
            fail( "Expected HostDisconnectedException" );
        }
        catch ( HostDisconnectedException e )
        {
        }
    }


    @Test
    public void testGetContainerInfoByHostname() throws Exception
    {
        ContainerHostInfo existingHost = registry.getContainerHostInfoByHostname( CONTAINER_HOSTNAME );
        assertNotNull( existingHost );

        try
        {
            registry.getContainerHostInfoByHostname( DUMMY_HOSTNAME );
            fail( "Expected HostDisconnectedException" );
        }
        catch ( HostDisconnectedException e )
        {
        }
    }


    @Test
    public void testGetContainersInfo() throws Exception
    {

        Set<ContainerHostInfo> info = registry.getContainerHostsInfo();

        assertFalse( info.isEmpty() );
        assertTrue( info.contains( containerHostInfo ) );
    }


    @Test
    public void testGetResourceHostInfoById() throws Exception
    {
        ResourceHostInfo existingHost = registry.getResourceHostInfoById( HOST_ID );
        assertNotNull( existingHost );

        try
        {
            registry.getResourceHostInfoById( UUID.randomUUID() );
            fail( "Expected HostDisconnectedException" );
        }
        catch ( HostDisconnectedException e )
        {
        }
    }


    @Test
    public void testGetResourceHostInfoByHostname() throws Exception
    {
        ResourceHostInfo existingHost = registry.getResourceHostInfoByHostname( HOST_HOSTNAME );

        assertNotNull( existingHost );

        try
        {
            ResourceHostInfo nonExistingHost = registry.getResourceHostInfoByHostname( DUMMY_HOSTNAME );
            fail( "Expected HostDisconnectedException" );
        }
        catch ( HostDisconnectedException e )
        {
        }
    }


    @Test
    public void testGetResourceHostsInfo() throws Exception
    {
        Set<ResourceHostInfo> info = registry.getResourceHostsInfo();

        assertFalse( info.isEmpty() );
        assertTrue( info.contains( resourceHostInfo ) );
    }


    @Test
    public void testGetParentByChild() throws Exception
    {
        ResourceHostInfo existingHost = registry.getResourceHostByContainerHost( containerHostInfo );

        assertNotNull( existingHost );


        ContainerHostInfo containerHostInfo1 = mock( ContainerHostInfo.class );
        when( containerHostInfo1.getId() ).thenReturn( UUID.randomUUID() );
        try
        {
            registry.getResourceHostByContainerHost( containerHostInfo1 );
            fail( "Expected HostDisconnectedException" );
        }
        catch ( HostDisconnectedException e )
        {
        }
    }


    @Test
    public void testAddHostListener() throws Exception
    {
        registry.addHostListener( hostListener );

        verify( hostListeners ).add( hostListener );
    }


    @Test
    public void testRemoveHostListener() throws Exception
    {
        registry.removeHostListener( hostListener );

        verify( hostListeners ).remove( hostListener );
    }


    @Test
    public void testRegisterHost() throws Exception
    {

        registry.registerHost( resourceHostInfo );

        verify( hosts ).put( resourceHostInfo.getId(), resourceHostInfo );
        verify( notifier ).execute( isA( HostNotifier.class ) );
    }


    @Test
    public void testDispose() throws Exception
    {
        registry.dispose();

        verify( hosts ).invalidateAll();
    }
}
