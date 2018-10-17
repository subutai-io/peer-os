package io.subutai.core.localpeer.impl.entity;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.Template;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.bazaar.share.quota.ContainerQuota;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ContainerHostEntityTest
{
    private static final ContainerHostState CONTAINER_HOST_STATE = ContainerHostState.RUNNING;
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String HOSTNAME = "hostname";
    private static final HostArchitecture ARCH = HostArchitecture.AMD64;
    private static final String INTERFACE_NAME = "eth0";
    private static final String IP = "127.0.0.1";
    private static final int PID = 123;
    private static final String TEMPLATE_NAME = "master";


    @Mock
    LocalPeer localPeer;

    @Mock
    ContainerHostInfo containerHostInfo;

    @Mock
    Peer peer;

    @Mock
    HostInterfaceModel anHostInterface;

    @Mock
    ResourceHost resourceHost;

    @Mock
    private HostInterfaces hostInterfaces;

    @Mock
    HostRegistry hostRegistry;

    @Mock
    Template template;

    private ContainerHostEntity containerHostEntity;


    @Before
    public void setUp() throws Exception
    {
        when( containerHostInfo.getId() ).thenReturn( HOST_ID );
        when( containerHostInfo.getHostname() ).thenReturn( HOSTNAME );
        when( containerHostInfo.getArch() ).thenReturn( ARCH );
        when( containerHostInfo.getState() ).thenReturn( CONTAINER_HOST_STATE );
        when( hostInterfaces.getAll() ).thenReturn( Sets.newHashSet( anHostInterface ) );
        when( containerHostInfo.getHostInterfaces() ).thenReturn( hostInterfaces );
        when( containerHostInfo.getState() ).thenReturn( CONTAINER_HOST_STATE );
        when( anHostInterface.getName() ).thenReturn( INTERFACE_NAME );
        when( anHostInterface.getIp() ).thenReturn( IP );
        when( hostRegistry.getHostInfoById( anyString() ) ).thenReturn( containerHostInfo );

        containerHostEntity = spy( new ContainerHostEntity() );
        doReturn( peer ).when( containerHostEntity ).getPeer();
        doReturn( localPeer ).when( containerHostEntity ).getLocalPeer();
        doReturn( template ).when( localPeer ).getTemplateById( anyString() );
        doReturn( TEMPLATE_NAME ).when( template ).getName();
    }


    @Test()
    public void testGetTemplateName() throws Exception
    {
        assertEquals( TEMPLATE_NAME, containerHostEntity.getTemplateName() );
    }


    @Test
    public void testGetTemplate() throws Exception
    {
        assertEquals( template, containerHostEntity.getTemplate() );
    }


    @Test
    public void testIsLocal() throws Exception
    {
        assertTrue( containerHostEntity.isLocal() );
    }


    @Test
    public void testGetState() throws Exception
    {
        doReturn( CONTAINER_HOST_STATE ).when( peer ).getContainerState( any( ContainerId.class ) );

        assertEquals( CONTAINER_HOST_STATE, containerHostEntity.getState() );
    }


    @Test
    public void testGetNSetParent() throws Exception
    {
        containerHostEntity.setParent( resourceHost );

        assertEquals( resourceHost, containerHostEntity.getParent() );
    }


    @Test
    public void testStart() throws Exception
    {
        containerHostEntity.start();

        verify( peer ).startContainer( containerHostEntity.getContainerId() );
    }


    @Test
    public void testStop() throws Exception
    {
        containerHostEntity.stop();

        verify( peer ).stopContainer( containerHostEntity.getContainerId() );
    }


    @Test
    public void testUpdateHostInfo() throws Exception
    {
        containerHostEntity.updateHostInfo( containerHostInfo );

        assertEquals( containerHostInfo.getContainerName(), containerHostEntity.getContainerName() );
    }


    @Test
    public void testGetQuota() throws Exception
    {
        containerHostEntity.getQuota();

        verify( peer ).getQuota( containerHostEntity.getContainerId() );
    }


    @Test
    public void testSetQuota() throws Exception
    {
        ContainerQuota ramQuota = mock( ContainerQuota.class );
        containerHostEntity.setQuota( ramQuota );

        verify( peer ).setQuota( containerHostEntity.getContainerId(), ramQuota );
    }
}
