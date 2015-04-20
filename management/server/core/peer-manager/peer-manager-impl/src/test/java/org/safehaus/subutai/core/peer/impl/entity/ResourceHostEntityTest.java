package org.safehaus.subutai.core.peer.impl.entity;


import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.host.HostArchitecture;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.host.Interface;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import com.google.common.collect.Sets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ResourceHostEntityTest
{
    private static final UUID PEER_ID = UUID.randomUUID();
    private static final UUID HOST_ID = UUID.randomUUID();
    private static final String HOSTNAME = "hostname";
    private static final HostArchitecture ARCH = HostArchitecture.AMD64;
    private static final String INTERFACE_NAME = "eth0";
    private static final String IP = "127.0.0.1";
    private static final String MAC = "mac";
    @Mock
    ContainerHost containerHost;
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
}
