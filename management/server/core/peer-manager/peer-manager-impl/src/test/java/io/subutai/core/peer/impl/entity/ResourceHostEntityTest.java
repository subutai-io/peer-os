package io.subutai.core.peer.impl.entity;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.host.HostArchitecture;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.host.Interface;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.protocol.Template;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.peer.api.ContainerState;
import io.subutai.core.peer.api.HostNotFoundException;
import io.subutai.core.peer.api.ResourceHostException;

import io.subutai.core.registry.api.TemplateRegistry;

import com.google.common.collect.Sets;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ResourceHostEntityTest
{
    private static final UUID PEER_ID = UUID.randomUUID();
    private static final UUID HOST_ID = UUID.randomUUID();
    private static final UUID CONTAINER_HOST_ID = UUID.randomUUID();
    private static final String CONTAINER_HOST_NAME = "hostname";
    private static final String TEMPLATE_NAME = "master";
    private static final String GATEWAY = "192.168.1.1";
    private static final int VLAN = 100;
    private static final int TIMEOUT = 120;
    private static final String HOSTNAME = "hostname";
    private static final HostArchitecture ARCH = HostArchitecture.AMD64;
    private static final String INTERFACE_NAME = "eth0";
    private static final String IP = "127.0.0.1";
    private static final String MAC = "mac";
    @Mock
    ContainerHostEntity containerHost;
    @Mock
    ExecutorService singleThreadExecutorService;
    @Mock
    Monitor monitor;
    @Mock
    CommandUtil commandUtil;
    @Mock
    TemplateRegistry registry;
    @Mock
    HostRegistry hostRegistry;
    @Mock
    Peer peer;
    @Mock
    HostInfo hostInfo;
    @Mock
    Interface anInterface;
    @Mock
    Callable callable;
    @Mock
    CommandResult commandResult;
    @Mock
    Future future;
    @Mock
    Template template;


    ResourceHostEntity resourceHostEntity;


    @Before
    public void setUp() throws Exception
    {
        when( hostInfo.getId() ).thenReturn( HOST_ID );
        when( hostInfo.getHostname() ).thenReturn( HOSTNAME );
        when( hostInfo.getArch() ).thenReturn( ARCH );
        when( hostInfo.getInterfaces() ).thenReturn( Sets.newHashSet( anInterface ) );
        when( anInterface.getInterfaceName() ).thenReturn( INTERFACE_NAME );
        when( anInterface.getIp() ).thenReturn( IP );
        when( anInterface.getMac() ).thenReturn( MAC );
        resourceHostEntity = new ResourceHostEntity( PEER_ID.toString(), hostInfo );
        resourceHostEntity.setHostRegistry( hostRegistry );
        resourceHostEntity.setMonitor( monitor );
        resourceHostEntity.setRegistry( registry );
        resourceHostEntity.setPeer( peer );
        resourceHostEntity.singleThreadExecutorService = singleThreadExecutorService;
        resourceHostEntity.commandUtil = commandUtil;
        when( containerHost.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( containerHost.getHostname() ).thenReturn( CONTAINER_HOST_NAME );
        when( commandUtil.execute( any( RequestBuilder.class ), eq( resourceHostEntity ) ) )
                .thenReturn( commandResult );
        when( singleThreadExecutorService.submit( any( Callable.class ) ) ).thenReturn( future );
    }


    @Test
    public void testDispose() throws Exception
    {
        resourceHostEntity.dispose();

        verify( singleThreadExecutorService ).shutdown();
    }


    @Test
    public void testQueueSequentialTask() throws Exception
    {
        resourceHostEntity.queueSequentialTask( callable );

        verify( singleThreadExecutorService ).submit( callable );
    }


    @Test
    public void testAddContainerHost() throws Exception
    {
        resourceHostEntity.addContainerHost( containerHost );

        verify( containerHost ).setParent( resourceHostEntity );
    }


    @Test
    public void testGetContainerHostState() throws Exception
    {
        try
        {
            resourceHostEntity.getContainerHostState( containerHost );
            fail( "Expected ResourceHostException" );
        }
        catch ( ResourceHostException e )
        {
        }


        when( commandResult.getStdOut() ).thenReturn( "State:RUNNING" );
        resourceHostEntity.addContainerHost( containerHost );

        ContainerState state = resourceHostEntity.getContainerHostState( containerHost );

        assertEquals( ContainerState.RUNNING, state );


        when( commandResult.getStdOut() ).thenReturn( "" );

        state = resourceHostEntity.getContainerHostState( containerHost );

        assertEquals( ContainerState.UNKNOWN, state );

        doThrow( new CommandException( "" ) ).when( commandUtil )
                                             .execute( any( RequestBuilder.class ), eq( resourceHostEntity ) );

        try
        {
            resourceHostEntity.getContainerHostState( containerHost );
            fail( "Expected ResourceHostException" );
        }
        catch ( ResourceHostException e )
        {
        }
    }


    @Test( expected = ResourceHostException.class )
    public void testGetResourceHostMetric() throws Exception
    {
        resourceHostEntity.getHostMetric();

        verify( monitor ).getResourceHostMetric( resourceHostEntity );

        doThrow( new MonitorException( "" ) ).when( monitor ).getResourceHostMetric( resourceHostEntity );

        resourceHostEntity.getHostMetric();
    }


    @Test
    public void testGetContainerHosts() throws Exception
    {
        resourceHostEntity.addContainerHost( containerHost );

        Set<ContainerHost> containerHosts = resourceHostEntity.getContainerHosts();

        assertTrue( containerHosts.contains( containerHost ) );
    }


    @Test
    public void testStartContainerHost() throws Exception
    {

        try
        {
            resourceHostEntity.startContainerHost( containerHost );
            fail( "Expected ResourceHostException" );
        }
        catch ( ResourceHostException e )
        {
        }

        when( containerHost.isConnected() ).thenReturn( true );
        when( commandResult.getStdOut() ).thenReturn( "State:RUNNING" );
        resourceHostEntity.addContainerHost( containerHost );

        resourceHostEntity.startContainerHost( containerHost );

        verify( commandUtil, atLeastOnce() ).execute( any( RequestBuilder.class ), eq( resourceHostEntity ) );

        when( commandResult.getStdOut() ).thenReturn( "State:STOPPED" );

        try
        {
            resourceHostEntity.startContainerHost( containerHost );
            fail( "Expected ResourceHostException" );
        }
        catch ( ResourceHostException e )
        {
        }

        when( commandResult.getStdOut() ).thenReturn( "State:RUNNING" );

        doThrow( new CommandException( "" ) ).when( commandUtil )
                                             .execute( any( RequestBuilder.class ), eq( resourceHostEntity ) );

        try
        {
            resourceHostEntity.startContainerHost( containerHost );
            fail( "Expected ResourceHostException" );
        }
        catch ( ResourceHostException e )
        {
        }
    }


    @Test
    public void testStopContainerHost() throws Exception
    {

        try
        {
            resourceHostEntity.stopContainerHost( containerHost );
            fail( "Expected ResourceHostException" );
        }
        catch ( ResourceHostException e )
        {
        }

        resourceHostEntity.addContainerHost( containerHost );

        resourceHostEntity.stopContainerHost( containerHost );

        verify( commandUtil, atLeastOnce() ).execute( any( RequestBuilder.class ), eq( resourceHostEntity ) );

        doThrow( new CommandException( "" ) ).when( commandUtil )
                                             .execute( any( RequestBuilder.class ), eq( resourceHostEntity ) );

        try
        {
            resourceHostEntity.stopContainerHost( containerHost );
            fail( "Expected ResourceHostException" );
        }
        catch ( ResourceHostException e )
        {
        }
    }


    @Test( expected = ResourceHostException.class )
    public void testDestroyContainerHost() throws Exception
    {
        try
        {
            resourceHostEntity.destroyContainerHost( containerHost );
            fail( "Expected ResourceHostException" );
        }
        catch ( ResourceHostException e )
        {
        }

        resourceHostEntity.addContainerHost( containerHost );

        resourceHostEntity.destroyContainerHost( containerHost );

        verify( future ).get();

        doThrow( new ExecutionException( null ) ).when( future ).get();


        resourceHostEntity.destroyContainerHost( containerHost );
    }


    @Test
    public void testGetContainerHostByName() throws Exception
    {
        try
        {
            resourceHostEntity.getContainerHostByName( HOSTNAME );
            fail( "Expected HostNotFoundException" );
        }
        catch ( HostNotFoundException e )
        {
        }

        resourceHostEntity.addContainerHost( containerHost );

        ContainerHost containerHost1 = resourceHostEntity.getContainerHostByName( HOSTNAME );

        assertEquals( containerHost, containerHost1 );
    }


    @Test
    public void testGetContainerHostById() throws Exception
    {

        try
        {
            resourceHostEntity.getContainerHostById( CONTAINER_HOST_ID );
            fail( "Expected HostNotFoundException" );
        }
        catch ( HostNotFoundException e )
        {
        }

        resourceHostEntity.addContainerHost( containerHost );

        ContainerHost containerHost1 = resourceHostEntity.getContainerHostById( CONTAINER_HOST_ID );

        assertEquals( containerHost, containerHost1 );
    }


    @Test( expected = ResourceHostException.class )
    public void testCreateContainer() throws Exception
    {


        try
        {
            resourceHostEntity.createContainer( TEMPLATE_NAME, HOSTNAME, IP, VLAN, GATEWAY, TIMEOUT );
            fail( "Expected HostNotFoundException" );
        }
        catch ( ResourceHostException e )
        {
        }


        when( registry.getTemplate( TEMPLATE_NAME ) ).thenReturn( template );
        resourceHostEntity.addContainerHost( containerHost );


        try
        {
            resourceHostEntity.createContainer( TEMPLATE_NAME, HOSTNAME, IP, VLAN, GATEWAY, TIMEOUT );
            fail( "Expected HostNotFoundException" );
        }
        catch ( ResourceHostException e )
        {
        }

        resourceHostEntity.removeContainerHost( containerHost );


        resourceHostEntity.createContainer( TEMPLATE_NAME, HOSTNAME, IP, VLAN, GATEWAY, TIMEOUT );

        verify( future ).get();

        doThrow( new ExecutionException( null ) ).when( future ).get();


        resourceHostEntity.createContainer( TEMPLATE_NAME, HOSTNAME, IP, VLAN, GATEWAY, TIMEOUT );
    }


    @Test
    public void testCreateContainer2() throws Exception
    {
        when( registry.getTemplate( TEMPLATE_NAME ) ).thenReturn( template );

        resourceHostEntity.createContainer( TEMPLATE_NAME, HOSTNAME, TIMEOUT );

        verify( future ).get();

    }
}

