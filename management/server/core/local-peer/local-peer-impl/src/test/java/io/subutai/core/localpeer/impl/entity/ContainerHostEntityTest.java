package io.subutai.core.localpeer.impl.entity;


import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
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
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.core.hostregistry.api.HostRegistry;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
@Ignore
public class ContainerHostEntityTest
{
    private static final ContainerHostState CONTAINER_HOST_STATE = ContainerHostState.RUNNING;
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String HOSTNAME = "hostname";
    private static final HostArchitecture ARCH = HostArchitecture.AMD64;
    private static final String INTERFACE_NAME = "eth0";
    private static final String IP = "127.0.0.1";
    private static final String TAG = "tag";
    private static final int PID = 123;
    private static final Set<Integer> CPU_SET = Sets.newHashSet( 1, 3, 5 );
    private static final String TEMPLATE_NAME = "master";
    private static final String TEMP_ARCH = "amd64";


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

    ContainerHostEntity containerHostEntity;
    @Mock
    private HostInterfaces hostInterfaces;

    @Mock
    HostRegistry hostRegistry;


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
        //        when( anHostInterface.getMac() ).thenReturn( MAC );
        when( hostRegistry.getHostInfoById( anyString() ) ).thenReturn( containerHostInfo );
    }


    @Test()
    public void testGetTemplateName() throws Exception
    {
        assertEquals( TEMPLATE_NAME, containerHostEntity.getTemplateName() );
    }


    @Test()
    public void testGetTemplateArch() throws Exception
    {
        assertEquals( TEMP_ARCH, containerHostEntity.getTemplateArch() );
    }


    @Test( expected = UnsupportedOperationException.class )
    public void testGetTemplate() throws Exception
    {
        containerHostEntity.getTemplate();
    }


    @Test
    public void testIsLocal() throws Exception
    {
        assertTrue( containerHostEntity.isLocal() );
    }


    @Test
    @Ignore
    public void testGetState() throws Exception
    {
        //        containerHostEntity.updateHostInfo( containerHostInfo );

        assertEquals( CONTAINER_HOST_STATE, containerHostEntity.getState() );
    }


    @Test
    public void testGetNSetParent() throws Exception
    {
        containerHostEntity.setParent( resourceHost );

        assertEquals( resourceHost, containerHostEntity.getParent() );
    }


    @Test
    public void testDispose() throws Exception
    {
        containerHostEntity.dispose();

        verify( peer ).destroyContainer( containerHostEntity.getContainerId() );
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
    @Ignore
    public void testUpdateHostInfo() throws Exception
    {
        containerHostEntity.updateHostInfo( containerHostInfo );

        assertEquals( CONTAINER_HOST_STATE, containerHostEntity.getState() );
    }


    @Test
    public void testGetProcessResourceUsage() throws Exception
    {
        containerHostEntity.getProcessResourceUsage( PID );

        verify( peer ).getProcessResourceUsage( containerHostEntity.getContainerId(), PID );
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
