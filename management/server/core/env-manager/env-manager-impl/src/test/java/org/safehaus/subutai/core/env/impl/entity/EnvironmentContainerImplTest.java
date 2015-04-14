package org.safehaus.subutai.core.env.impl.entity;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.environment.EnvironmentNotFoundException;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostArchitecture;
import org.safehaus.subutai.common.host.Interface;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.quota.RamQuota;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.impl.TestUtil;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentContainerImplTest
{

    @Mock
    Peer peer;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    DataService<String, EnvironmentContainerImpl> dataService;
    @Mock
    HostInfoModel hostInfoModel;
    @Mock
    Template template;
    @Mock
    Interface anInterface;
    @Mock
    Environment environment;
    @Mock
    EnvironmentNotFoundException environmentNotFoundException;
    @Mock
    RequestBuilder requestBuilder;
    @Mock
    CommandCallback callback;
    @Mock
    ProcessResourceUsage resourceUsage;
    @Mock
    RamQuota ramQuota;
    @Mock
    DiskQuota diskQuota;
    @Mock
    QuotaInfo quotaInfo;


    EnvironmentContainerImpl environmentContainer;


    @Before
    public void setUp() throws Exception
    {
        when( peer.getId() ).thenReturn( TestUtil.PEER_ID );
        when( hostInfoModel.getId() ).thenReturn( TestUtil.CONTAINER_ID );
        when( hostInfoModel.getHostname() ).thenReturn( TestUtil.HOSTNAME );
        when( hostInfoModel.getArch() ).thenReturn( HostArchitecture.AMD64 );
        when( anInterface.getIp() ).thenReturn( TestUtil.IP );
        when( anInterface.getInterfaceName() ).thenReturn( TestUtil.INTERFACE_NAME );
        when( anInterface.getMac() ).thenReturn( TestUtil.MAC );
        when( hostInfoModel.getInterfaces() ).thenReturn( Sets.newHashSet( anInterface ) );
        when( template.getTemplateName() ).thenReturn( TestUtil.TEMPLATE_NAME );
        environmentContainer =
                new EnvironmentContainerImpl( TestUtil.LOCAL_PEER_ID, peer, TestUtil.NODE_GROUP_NAME, hostInfoModel,
                        template, TestUtil.SSH_GROUP_ID, TestUtil.HOSTS_GROUP_ID, TestUtil.DEFAULT_DOMAIN );
        environmentContainer.setDataService( dataService );
        environmentContainer.setPeer( peer );
        environmentContainer.setEnvironmentManager( environmentManager );
        environmentContainer.setEnvironment( environment );
    }


    @Test
    public void testSetDefaultGateway() throws Exception
    {
        environmentContainer.setDefaultGateway( TestUtil.GATEWAY_IP );

        verify( peer ).setDefaultGateway( environmentContainer, TestUtil.GATEWAY_IP );
    }


    @Test
    public void testIsLocal() throws Exception
    {
        when( peer.isLocal() ).thenReturn( true );

        assertTrue( environmentContainer.isLocal() );
    }


    @Test
    public void testGetEnvironmentId() throws Exception
    {
        when( environment.getId() ).thenReturn( TestUtil.ENV_ID );

        assertEquals( TestUtil.ENV_ID.toString(), environmentContainer.getEnvironmentId() );
    }


    @Test
    public void testGetNodeGroupName() throws Exception
    {

        assertEquals( TestUtil.NODE_GROUP_NAME, environmentContainer.getNodeGroupName() );
    }


    @Test
    public void testGetState() throws Exception
    {
        when( peer.getContainerHostState( environmentContainer ) ).thenReturn( ContainerHostState.RUNNING );

        assertEquals( ContainerHostState.RUNNING, environmentContainer.getState() );
    }


    @Test( expected = PeerException.class )
    public void testDispose() throws Exception
    {
        environmentContainer.dispose();

        verify( environmentManager ).destroyContainer( environmentContainer, false, false );

        doThrow( environmentNotFoundException ).when( environmentManager )
                                               .destroyContainer( environmentContainer, false, false );

        environmentContainer.dispose();
    }


    @Test
    public void testDestroy() throws Exception
    {
        environmentContainer.destroy();

        verify( peer ).destroyContainer( environmentContainer );
    }


    @Test
    public void testStart() throws Exception
    {
        environmentContainer.start();

        verify( peer ).startContainer( environmentContainer );
    }


    @Test
    public void testStop() throws Exception
    {
        environmentContainer.stop();

        verify( peer ).stopContainer( environmentContainer );
    }


    @Test
    public void testGetPeer() throws Exception
    {
        assertEquals( peer, environmentContainer.getPeer() );
    }


    @Test
    public void testGetTemplate() throws Exception
    {
        when( peer.getTemplate( TestUtil.TEMPLATE_NAME ) ).thenReturn( template );

        assertEquals( template, environmentContainer.getTemplate() );
    }


    @Test
    public void testGetTemplateName() throws Exception
    {
        assertEquals( TestUtil.TEMPLATE_NAME, environmentContainer.getTemplateName() );
    }


    @Test
    public void testAddTag() throws Exception
    {
        environmentContainer.addTag( TestUtil.TAG );

        verify( dataService ).update( environmentContainer );
    }


    @Test
    public void testRemoveTag() throws Exception
    {
        environmentContainer.removeTag( TestUtil.TAG );

        verify( dataService ).update( environmentContainer );
    }


    @Test
    public void testGetTags() throws Exception
    {
        environmentContainer.addTag( TestUtil.TAG );

        assertTrue( environmentContainer.getTags().contains( TestUtil.TAG ) );
    }


    @Test
    public void testGetPeerId() throws Exception
    {
        assertEquals( TestUtil.PEER_ID.toString(), environmentContainer.getPeerId() );
    }


    @Test
    public void testGetHostId() throws Exception
    {
        assertEquals( TestUtil.CONTAINER_ID.toString(), environmentContainer.getHostId() );
    }


    @Test
    public void testGetHostname() throws Exception
    {
        assertEquals( TestUtil.HOSTNAME, environmentContainer.getHostname() );
    }


    @Test
    public void testSetHostname() throws Exception
    {
        environmentContainer.setHostname( TestUtil.HOSTNAME );

        assertEquals( TestUtil.HOSTNAME, environmentContainer.getHostname() );
    }


    @Test
    public void testExecute() throws Exception
    {
        environmentContainer.execute( requestBuilder );

        verify( peer ).execute( requestBuilder, environmentContainer );
    }


    @Test
    public void testExecute2() throws Exception
    {
        environmentContainer.execute( requestBuilder, callback );

        verify( peer ).execute( requestBuilder, environmentContainer, callback );
    }


    @Test
    public void testExecuteAsync() throws Exception
    {
        environmentContainer.executeAsync( requestBuilder );

        verify( peer ).executeAsync( requestBuilder, environmentContainer );
    }


    @Test
    public void testExecuteAsync2() throws Exception
    {
        environmentContainer.executeAsync( requestBuilder, callback );

        verify( peer ).executeAsync( requestBuilder, environmentContainer, callback );
    }


    @Test
    public void testIsConnected() throws Exception
    {
        when( peer.isConnected( environmentContainer ) ).thenReturn( true );

        assertTrue( environmentContainer.isConnected() );
    }


    @Test
    public void testGetNetInterfaces() throws Exception
    {
        assertFalse( environmentContainer.getNetInterfaces().isEmpty() );
    }


    @Test
    public void testGetIpByInterfaceName() throws Exception
    {
        assertEquals( TestUtil.IP, environmentContainer.getIpByInterfaceName( TestUtil.INTERFACE_NAME ) );
    }


    @Test
    public void testGetMacByInterfaceName() throws Exception
    {
        assertEquals( TestUtil.MAC, environmentContainer.getMacByInterfaceName( TestUtil.INTERFACE_NAME ) );
    }


    @Test
    public void testGetHostArchitecture() throws Exception
    {
        assertEquals( HostArchitecture.AMD64, environmentContainer.getHostArchitecture() );
    }


    @Test
    public void testGetProcessResourceUsage() throws Exception
    {
        when( peer.getProcessResourceUsage( environmentContainer, TestUtil.PID ) ).thenReturn( resourceUsage );


        assertEquals( resourceUsage, environmentContainer.getProcessResourceUsage( TestUtil.PID ) );
        verify( peer ).getProcessResourceUsage( environmentContainer, TestUtil.PID );
    }


    @Test
    public void testGetHostsGroupId() throws Exception
    {
        assertEquals( TestUtil.HOSTS_GROUP_ID, environmentContainer.getHostsGroupId() );
    }


    @Test
    public void testGetSshGroupId() throws Exception
    {
        assertEquals( TestUtil.SSH_GROUP_ID, environmentContainer.getSshGroupId() );
    }


    @Test
    public void testGetDomainName() throws Exception
    {
        assertEquals( TestUtil.DEFAULT_DOMAIN, environmentContainer.getDomainName() );
    }


    @Test
    public void testGetRamQuota() throws Exception
    {
        environmentContainer.getRamQuota();

        verify( peer ).getRamQuota( environmentContainer );
    }


    @Test
    public void testRamQuotaInfo() throws Exception
    {
        environmentContainer.getRamQuotaInfo();

        verify( peer ).getRamQuotaInfo( environmentContainer );
    }


    @Test
    public void testSetRamQuota() throws Exception
    {
        environmentContainer.setRamQuota( TestUtil.RAM_MB );

        verify( peer ).setRamQuota( environmentContainer, TestUtil.RAM_MB );

        environmentContainer.setRamQuota( ramQuota );

        verify( peer ).setRamQuota( environmentContainer, ramQuota );
    }


    @Test
    public void testGetCpuQuota() throws Exception
    {
        environmentContainer.getCpuQuota();

        verify( peer ).getCpuQuota( environmentContainer );
    }


    @Test
    public void testGetCpuQuotaInfo() throws Exception
    {
        environmentContainer.getCpuQuotaInfo();

        verify( peer ).getCpuQuotaInfo( environmentContainer );
    }


    @Test
    public void testSetCpuQuota() throws Exception
    {
        environmentContainer.setCpuQuota( TestUtil.CPU_QUOTA );

        verify( peer ).setCpuQuota( environmentContainer, TestUtil.CPU_QUOTA );
    }


    @Test
    public void testGetCpuSet() throws Exception
    {
        environmentContainer.getCpuSet();

        verify( peer ).getCpuSet( environmentContainer );
    }


    @Test
    public void testSetCpuSet() throws Exception
    {
        environmentContainer.setCpuSet( TestUtil.CPU_SET );

        verify( peer ).setCpuSet( environmentContainer, TestUtil.CPU_SET );
    }


    @Test
    public void testGetDiskQuota() throws Exception
    {
        environmentContainer.getDiskQuota( DiskPartition.VAR );

        verify( peer ).getDiskQuota( environmentContainer, DiskPartition.VAR );
    }


    @Test
    public void testSetDiskQuota() throws Exception
    {
        environmentContainer.setDiskQuota( diskQuota );

        verify( peer ).setDiskQuota( environmentContainer, diskQuota );
    }


    @Test
    public void testGetAvailableRamQuota() throws Exception
    {
        environmentContainer.getAvailableRamQuota();

        verify( peer ).getAvailableRamQuota( environmentContainer );
    }


    @Test
    public void testGetAvailableCpuQuota() throws Exception
    {
        environmentContainer.getAvailableCpuQuota();

        verify( peer ).getAvailableCpuQuota( environmentContainer );
    }


    @Test
    public void testGetAvailableDiskQuota() throws Exception
    {
        environmentContainer.getAvailableDiskQuota( DiskPartition.VAR );

        verify( peer ).getAvailableDiskQuota( environmentContainer, DiskPartition.VAR );
    }


    @Test( expected = UnsupportedOperationException.class )
    public void testGetQuota() throws Exception
    {
        environmentContainer.getQuota( QuotaType.QUOTA_TYPE_CPU );
    }


    @Test( expected = UnsupportedOperationException.class )
    public void testGetQuotaInfo() throws Exception
    {
        environmentContainer.getQuotaInfo( QuotaType.QUOTA_TYPE_CPU );
    }


    @Test( expected = UnsupportedOperationException.class )
    public void testSetQuota() throws Exception
    {
        environmentContainer.setQuota( quotaInfo );
    }


    @Test( expected = UnsupportedOperationException.class )
    public void testGetLastHeartBeat() throws Exception
    {
        environmentContainer.getLastHeartbeat();
    }


    @Test
    public void testEquals() throws Exception
    {
        EnvironmentContainerImpl environmentContainer2 = new EnvironmentContainerImpl();

        environmentContainer2.setHostId( TestUtil.CONTAINER_ID );

        assertEquals( environmentContainer2, environmentContainer );
    }


    @Test
    public void testHashCode() throws Exception
    {

        EnvironmentContainerImpl environmentContainer2 = new EnvironmentContainerImpl();

        environmentContainer2.setHostId( TestUtil.CONTAINER_ID );

        assertEquals( environmentContainer2.hashCode(), environmentContainer.hashCode() );
    }


    @Test
    public void testToString() throws Exception
    {
        String toString = environmentContainer.toString();

        verify( peer ).getContainerHostState( environmentContainer );

        assertThat( toString, containsString( TestUtil.CONTAINER_ID.toString() ) );
    }



}
