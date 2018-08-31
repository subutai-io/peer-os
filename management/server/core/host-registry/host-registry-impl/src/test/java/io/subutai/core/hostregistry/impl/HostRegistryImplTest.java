package io.subutai.core.hostregistry.impl;


import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.cache.Cache;
import com.google.common.collect.Sets;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.host.ResourceHostInfoModel;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.util.IPUtil;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostListener;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class HostRegistryImplTest
{

    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String HOST_HOSTNAME = "host";
    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_HOSTNAME = "container";
    private static final String DUMMY_HOSTNAME = "dummy";
    private static final String HOST_IP = "127.0.0.1";

    @Mock
    Set<HostListener> hostListeners;

    @Mock
    ExecutorService notifier;


    @Mock
    Cache<String, ResourceHostInfo> hosts;

    @Mock
    ConcurrentMap map;

    @Mock
    ResourceHostInfoModel resourceHostInfo;

    @Mock
    ContainerHostInfo containerHostInfo;

    @Mock
    HostListener hostListener;

    @Mock
    ScheduledExecutorService hostUpdater;

    @Mock
    QuotaAlertValue quotaAlertValue;

    @Mock
    IPUtil ipUtil;

    @Mock
    WebClient webClient;
    @Mock
    Response response;

    HostRegistryImpl registry;
    Set<QuotaAlertValue> alerts;


    @Before
    public void setUp() throws Exception
    {
        registry = spy( new HostRegistryImpl() );
        registry.hostListeners = hostListeners;
        registry.threadPool = notifier;
        registry.hosts = hosts;
        registry.hostUpdater = hostUpdater;
        registry.ipUtil = ipUtil;
        when( hosts.asMap() ).thenReturn( map );
        when( map.values() ).thenReturn( Sets.newHashSet( resourceHostInfo ) );
        when( resourceHostInfo.getContainers() ).thenReturn( Sets.newHashSet( containerHostInfo ) );
        when( resourceHostInfo.getId() ).thenReturn( HOST_ID );
        when( resourceHostInfo.getHostname() ).thenReturn( HOST_HOSTNAME );
        when( containerHostInfo.getId() ).thenReturn( CONTAINER_ID );
        when( containerHostInfo.getHostname() ).thenReturn( CONTAINER_HOSTNAME );
        when( containerHostInfo.getContainerName() ).thenReturn( CONTAINER_HOSTNAME );
        Iterator<HostListener> hostListenerIterator = mock( Iterator.class );
        when( hostListeners.iterator() ).thenReturn( hostListenerIterator );
        when( hostListenerIterator.hasNext() ).thenReturn( true ).thenReturn( false );
        when( hostListenerIterator.next() ).thenReturn( hostListener );
        alerts = Sets.newHashSet( quotaAlertValue );
        doReturn( webClient ).when( registry ).getWebClient( anyString(), anyString() );
        doReturn( response ).when( webClient ).get();
    }


    @Test
    public void testGetContainerInfoById() throws Exception
    {
        ContainerHostInfo existingHost = registry.getContainerHostInfoById( CONTAINER_ID );

        assertNotNull( existingHost );

        try
        {
            registry.getContainerHostInfoById( UUID.randomUUID().toString() );
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
            registry.getHostInfoById( UUID.randomUUID().toString() );
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
            registry.getResourceHostInfoById( UUID.randomUUID().toString() );
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
        when( containerHostInfo1.getId() ).thenReturn( UUID.randomUUID().toString() );
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

        registry.registerHost( resourceHostInfo, alerts );

        verify( hosts ).put( resourceHostInfo.getId(), resourceHostInfo );
        verify( notifier ).execute( isA( HostNotifier.class ) );
    }


    @Test
    public void testDispose() throws Exception
    {
        registry.dispose();

        verify( hosts ).invalidateAll();
    }


    @Test
    public void testUpdateResourceHostEntryTimestamp() throws Exception
    {
        registry.updateResourceHostEntryTimestamp( HOST_ID );

        verify( hosts ).getIfPresent( HOST_ID );
    }


    @Test
    public void testRemoveResourceHost() throws Exception
    {
        doReturn( resourceHostInfo ).when( hosts ).getIfPresent( HOST_ID );

        registry.removeResourceHost( HOST_ID );

        verify( hosts ).invalidate( HOST_ID );
    }


    @Test
    public void testInit() throws Exception
    {
        registry.init();

        verify( hostUpdater )
                .scheduleWithFixedDelay( any( Runnable.class ), anyLong(), anyLong(), any( TimeUnit.class ) );

        assertNotSame( hosts, registry.hosts );
    }


    @Test
    public void testUpdateHosts() throws Exception
    {

        LocalPeer localPeer = mock( LocalPeer.class );
        ResourceHost resourceHost = mock( ResourceHost.class );
        doReturn( localPeer ).when( registry ).getLocalPeer();
        doReturn( Sets.newHashSet( resourceHost ) ).when( localPeer ).getResourceHosts();

        registry.updateHosts();

        verify( registry, times( 2 ) ).checkAndUpdateHosts( anySet() );
    }


    @Test
    public void testUpdateHost() throws Exception
    {
        WebClient webClient = mock( WebClient.class );
        doReturn( webClient ).when( registry ).getWebClient( anyString(), anyString() );
        Response response = mock( Response.class );
        doReturn( response ).when( webClient ).get();
        doReturn( Response.Status.OK.getStatusCode() ).when( response ).getStatus();

        registry.updateHost( resourceHostInfo );

        verify( registry ).updateResourceHostEntryTimestamp( HOST_ID );

        ResourceHost resourceHost = mock( ResourceHost.class );
        doReturn( HOST_ID ).when( resourceHost ).getId();

        registry.updateHost( resourceHost );

        verify( registry ).getResourceHostInfoById( HOST_ID );

        doThrow( new HostDisconnectedException( null ) ).when( registry ).getResourceHostInfoById( HOST_ID );

        registry.updateHost( resourceHost );

        verify( registry ).requestHeartbeat( resourceHost );
    }


    @Test
    public void testPingHost()
    {
        registry.pingHost( HOST_IP );

        verify( webClient ).get();

        doReturn( Response.Status.OK.getStatusCode() ).when( response ).getStatus();

        assertTrue( registry.pingHost( HOST_IP ) );

        doReturn( Response.Status.SERVICE_UNAVAILABLE.getStatusCode() ).when( response ).getStatus();

        assertFalse( registry.pingHost( HOST_IP ) );

        doThrow( new RuntimeException() ).when( webClient ).get();

        assertFalse( registry.pingHost( HOST_IP ) );
    }


    @Test(expected = HostDisconnectedException.class)
    public void testGetContainerHostInfoByContainerName() throws Exception
    {
        assertNotNull( registry.getContainerHostInfoByContainerName( CONTAINER_HOSTNAME ) );

        doReturn( "" ).when( containerHostInfo ).getContainerName();

        registry.getContainerHostInfoByContainerName( CONTAINER_HOSTNAME );
    }
}
