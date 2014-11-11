package org.safehaus.subutai.core.hostregistry.impl;


import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.broker.api.Broker;
import org.safehaus.subutai.core.broker.api.BrokerException;
import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostRegistryException;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostListener;

import com.google.common.cache.Cache;
import com.google.common.collect.Sets;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
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

    @Mock
    Broker broker;

    @Mock
    Set<HostListener> hostListeners;

    @Mock
    Executor notifier;

    @Mock
    HeartBeatListener heartBeatListener;

    @Mock
    Cache<UUID, HostInfo> hosts;

    @Mock
    ConcurrentMap map;

    @Mock
    HostInfo hostInfo;

    @Mock
    ContainerHostInfo containerHostInfo;

    @Mock
    HostListener hostListener;

    HostRegistryImpl registry;


    @Before
    public void setUp() throws Exception
    {
        registry = new HostRegistryImpl( broker );
        registry.hostListeners = hostListeners;
        registry.notifier = notifier;
        registry.heartBeatListener = heartBeatListener;
        registry.hosts = hosts;
        when( hosts.asMap() ).thenReturn( map );
        when( map.values() ).thenReturn( Sets.newHashSet( hostInfo ) );
        when( hostInfo.getContainers() ).thenReturn( Sets.newHashSet( containerHostInfo ) );
        when( hostInfo.getId() ).thenReturn( HOST_ID );
        when( hostInfo.getHostname() ).thenReturn( HOST_HOSTNAME );
        when( containerHostInfo.getId() ).thenReturn( CONTAINER_ID );
        when( containerHostInfo.getHostname() ).thenReturn( CONTAINER_HOSTNAME );
        Iterator<HostListener> hostListenerIterator = mock( Iterator.class );
        when( hostListeners.iterator() ).thenReturn( hostListenerIterator );
        when( hostListenerIterator.hasNext() ).thenReturn( true ).thenReturn( false );
        when( hostListenerIterator.next() ).thenReturn( hostListener );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new HostRegistryImpl( null );
    }


    @Test
    public void testGetContainerInfoById() throws Exception
    {
        ContainerHostInfo existingHost = registry.getContainerInfoById( CONTAINER_ID );
        ContainerHostInfo nonExistingHost = registry.getContainerInfoById( UUID.randomUUID() );

        assertNotNull( existingHost );
        assertNull( nonExistingHost );
    }


    @Test
    public void testGetContainerInfoByHostname() throws Exception
    {
        ContainerHostInfo existingHost = registry.getContainerInfoByHostname( CONTAINER_HOSTNAME );
        ContainerHostInfo nonExistingHost = registry.getContainerInfoByHostname( DUMMY_HOSTNAME );

        assertNotNull( existingHost );
        assertNull( nonExistingHost );
    }


    @Test
    public void testGetContainersInfo() throws Exception
    {

        Set<ContainerHostInfo> info = registry.getContainersInfo();

        assertFalse( info.isEmpty() );
        assertTrue( info.contains( containerHostInfo ) );
    }


    @Test
    public void testGetHostInfoById() throws Exception
    {
        HostInfo existingHost = registry.getHostInfoById( HOST_ID );
        HostInfo nonExistingHost = registry.getHostInfoById( UUID.randomUUID() );

        assertNotNull( existingHost );
        assertNull( nonExistingHost );
    }


    @Test
    public void testGetHostInfoByHostname() throws Exception
    {
        HostInfo existingHost = registry.getHostInfoByHostname( HOST_HOSTNAME );
        HostInfo nonExistingHost = registry.getHostInfoByHostname( DUMMY_HOSTNAME );

        assertNotNull( existingHost );
        assertNull( nonExistingHost );
    }


    @Test
    public void testGetHostsInfo() throws Exception
    {
        Set<HostInfo> info = registry.getHostsInfo();

        assertFalse( info.isEmpty() );
        assertTrue( info.contains( hostInfo ) );
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

        registry.registerHost( hostInfo );

        verify( hosts ).put( hostInfo.getId(), hostInfo );
        verify( notifier ).execute( isA( HostNotifier.class ) );
    }


    @Test
    public void testInit() throws Exception
    {
        registry.init();

        verify( broker ).addByteMessageListener( heartBeatListener );


        doThrow( new BrokerException( "" ) ).when( broker ).addByteMessageListener( any( ByteMessageListener.class ) );
        try
        {
            registry.init();
            fail( "Expected ContainerRegistryException" );
        }
        catch ( HostRegistryException e )
        {
        }
    }


    @Test
    public void testDispose() throws Exception
    {
        registry.dispose();

        verify( broker ).removeMessageListener( heartBeatListener );
        verify( hosts ).invalidateAll();
    }
}
