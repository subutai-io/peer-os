package org.safehaus.subutai.core.peer.impl.entity;


import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostArchitecture;
import org.safehaus.subutai.common.host.Interface;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.RamQuota;
import io.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.peer.api.ContainerGroup;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ContainerHostEntityTest
{
    private static final ContainerHostState CONTAINER_HOST_STATE = ContainerHostState.RUNNING;
    private static final UUID ENVIRONMENT_ID = UUID.randomUUID();
    private static final UUID PEER_ID = UUID.randomUUID();
    private static final UUID HOST_ID = UUID.randomUUID();
    private static final String HOSTNAME = "hostname";
    private static final HostArchitecture ARCH = HostArchitecture.AMD64;
    private static final String INTERFACE_NAME = "eth0";
    private static final String IP = "127.0.0.1";
    private static final String MAC = "mac";
    private static final String TAG = "tag";
    private static final String GATEWAY_IP = "127.0.0.1";
    private static final int PID = 123;
    private static final int RAM_QUOTA = 2048;
    private static final int CPU_QUOTA = 100;
    private static final Set<Integer> CPU_SET = Sets.newHashSet( 1, 3, 5 );

    @Mock
    DataService dataService;
    @Mock
    LocalPeer localPeer;
    @Mock
    ContainerGroup containerGroup;
    @Mock
    ContainerHostInfo containerHostInfo;
    @Mock
    Peer peer;
    @Mock
    Interface anInterface;
    @Mock
    ResourceHost resourceHost;

    ContainerHostEntity containerHostEntity;


    @Before
    public void setUp() throws Exception
    {
        when( containerHostInfo.getId() ).thenReturn( HOST_ID );
        when( containerHostInfo.getHostname() ).thenReturn( HOSTNAME );
        when( containerHostInfo.getArch() ).thenReturn( ARCH );
        when( containerHostInfo.getInterfaces() ).thenReturn( Sets.newHashSet( anInterface ) );
        when( containerHostInfo.getStatus() ).thenReturn( CONTAINER_HOST_STATE );
        when( anInterface.getInterfaceName() ).thenReturn( INTERFACE_NAME );
        when( anInterface.getIp() ).thenReturn( IP );
        when( anInterface.getMac() ).thenReturn( MAC );

        containerHostEntity = new ContainerHostEntity( PEER_ID.toString(), containerHostInfo );
        containerHostEntity.setLocalPeer( localPeer );
        containerHostEntity.setDataService( dataService );
        containerHostEntity.setPeer( peer );
        when( localPeer.findContainerGroupByContainerId( HOST_ID ) ).thenReturn( containerGroup );
        when( containerGroup.getEnvironmentId() ).thenReturn( ENVIRONMENT_ID );
    }


    @Test( expected = UnsupportedOperationException.class )
    public void testGetNodeGroupName() throws Exception
    {
        containerHostEntity.getNodeGroupName();
    }


    @Test( expected = UnsupportedOperationException.class )
    public void testGetTemplateName() throws Exception
    {
        containerHostEntity.getTemplateName();
    }


    @Test( expected = UnsupportedOperationException.class )
    public void testGetTemplate() throws Exception
    {
        containerHostEntity.getTemplate();
    }


    @Test
    public void testGetEnvironmentId() throws Exception
    {
        assertEquals( ENVIRONMENT_ID.toString(), containerHostEntity.getEnvironmentId() );
    }


    @Test
    public void testAddTag() throws Exception
    {
        containerHostEntity.addTag( TAG );

        verify( dataService ).update( containerHostEntity );
    }


    @Test
    public void testRemoveTag() throws Exception
    {
        containerHostEntity.removeTag( TAG );

        verify( dataService ).update( containerHostEntity );
    }


    @Test
    public void testGetTags() throws Exception
    {
        containerHostEntity.addTag( TAG );

        assertTrue( containerHostEntity.getTags().contains( TAG ) );
    }


    @Test
    public void testSetDefaultGateway() throws Exception
    {
        containerHostEntity.setDefaultGateway( GATEWAY_IP );

        verify( peer ).setDefaultGateway( containerHostEntity, GATEWAY_IP );
    }


    @Test
    public void testIsLocal() throws Exception
    {
        assertTrue( containerHostEntity.isLocal() );
    }


    @Test
    public void testGetState() throws Exception
    {
        containerHostEntity.updateHostInfo( containerHostInfo );

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

        verify( peer ).destroyContainer( containerHostEntity );
    }


    @Test
    public void testStart() throws Exception
    {
        containerHostEntity.start();

        verify( peer ).startContainer( containerHostEntity );
    }


    @Test
    public void testStop() throws Exception
    {
        containerHostEntity.stop();

        verify( peer ).stopContainer( containerHostEntity );
    }


    @Test
    public void testUpdateHostInfo() throws Exception
    {
        containerHostEntity.updateHostInfo( containerHostInfo );

        assertEquals( CONTAINER_HOST_STATE, containerHostEntity.getState() );
    }


    @Test
    public void testGetProcessResourceUsage() throws Exception
    {
        containerHostEntity.getProcessResourceUsage( PID );

        verify( peer ).getProcessResourceUsage( containerHostEntity, PID );
    }


    @Test
    public void testGetRamQuota() throws Exception
    {
        containerHostEntity.getRamQuota();

        verify( peer ).getRamQuota( containerHostEntity );
    }


    @Test
    public void testSetRamQuota() throws Exception
    {
        containerHostEntity.setRamQuota( RAM_QUOTA );

        verify( peer ).setRamQuota( containerHostEntity, RAM_QUOTA );


        RamQuota ramQuota = mock( RamQuota.class );

        containerHostEntity.setRamQuota( ramQuota );

        verify( peer ).setRamQuota( containerHostEntity, ramQuota );
    }


    @Test
    public void testGetRamQuotaInfo() throws Exception
    {
        containerHostEntity.getRamQuotaInfo();

        verify( peer ).getRamQuotaInfo( containerHostEntity );
    }


    @Test
    public void testGetCpuQuota() throws Exception
    {
        containerHostEntity.getCpuQuota();

        verify( peer ).getCpuQuota( containerHostEntity );
    }


    @Test
    public void testGetCpuQuotaInfo() throws Exception
    {
        containerHostEntity.getCpuQuotaInfo();

        verify( peer ).getCpuQuotaInfo( containerHostEntity );
    }


    @Test
    public void testSetCpuQuota() throws Exception
    {
        containerHostEntity.setCpuQuota( CPU_QUOTA );

        verify( peer ).setCpuQuota( containerHostEntity, CPU_QUOTA );
    }


    @Test
    public void testGetCpuSet() throws Exception
    {
        containerHostEntity.getCpuSet();


        verify( peer ).getCpuSet( containerHostEntity );
    }


    @Test
    public void testSetCpuSet() throws Exception
    {
        containerHostEntity.setCpuSet( CPU_SET );

        verify( peer ).setCpuSet( containerHostEntity, CPU_SET );
    }


    @Test
    public void testGetDiskQuota() throws Exception
    {
        DiskPartition diskPartition = DiskPartition.VAR;

        containerHostEntity.getDiskQuota( diskPartition );

        verify( peer ).getDiskQuota( containerHostEntity, diskPartition );
    }


    @Test
    public void testSetDiskQuota() throws Exception
    {
        DiskQuota diskQuota = mock( DiskQuota.class );

        containerHostEntity.setDiskQuota( diskQuota );

        verify( peer ).setDiskQuota( containerHostEntity, diskQuota );
    }


    @Test
    public void testGetAvailableRamQuota() throws Exception
    {
        containerHostEntity.getAvailableRamQuota();

        verify( peer ).getAvailableRamQuota( containerHostEntity );
    }


    @Test
    public void testGetAvailableCpuQuota() throws Exception
    {
        containerHostEntity.getAvailableCpuQuota();

        verify( peer ).getAvailableCpuQuota( containerHostEntity );
    }


    @Test
    public void testGetAvailableDiskQuota() throws Exception
    {
        DiskPartition diskPartition = DiskPartition.VAR;

        containerHostEntity.getAvailableDiskQuota( diskPartition );

        verify( peer ).getAvailableDiskQuota( containerHostEntity, diskPartition );
    }
}
