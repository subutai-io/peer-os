package io.subutai.core.lxc.quota.impl;


import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandResult;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.quota.ContainerCpuResource;
import io.subutai.common.quota.ContainerHomeResource;
import io.subutai.common.quota.ContainerRamResource;
import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;
import io.subutai.common.resource.ByteUnit;
import io.subutai.core.lxc.quota.impl.parser.DiskValueResourceParser;
import io.subutai.core.lxc.quota.impl.parser.CpuResourceValueParser;
import io.subutai.core.peer.api.PeerManager;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@Ignore
@RunWith( MockitoJUnitRunner.class )
public class QuotaManagerImplTest
{
    private static final String CONTAINER_HOST_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_HOST_NAME = "container_host_name";
    private QuotaManagerImpl quotaManager;
    private Commands commands = new Commands();

    //    private String uuid = UUID.randomUUID().toString();
    //    private String containerName = "containerName";
    private String containerHostname = "containerHostname";
    private String quotaResult = "100";
    private String cpuSetCommandOutput = "1-5,7";
    private String cpuSetString = "1,2,3,4,5,7";

    private Set<Integer> cpuSet = Sets.newHashSet( 1, 2, 3, 4, 5, 7 );

    private ContainerRamResource ramQuotaInfo =
            new ContainerRamResource( new ByteValueResource( ByteValueResource.toBytes( "16", ByteUnit.MB ) ) );
    private ByteValueResource ramQuotaValue = DiskValueResourceParser.getInstance().parse( ByteValueResource
            .toBytes( "16", ByteUnit.MB ).toPlainString() );
    private ContainerCpuResource cpuQuota =
            new ContainerCpuResource( CpuResourceValueParser.getInstance().parse( quotaResult ) );
    private ContainerHomeResource diskQuota =
            new ContainerHomeResource( new ByteValueResource( ByteValueResource.toBytes( "2", ByteUnit.TB ) ) );

    @Mock
    ResourceHost resourceHost;

    @Mock
    LocalPeer localPeer;
    @Mock
    PeerManager peerManager;

    @Mock
    ContainerHost containerHost;

    @Mock
    CommandResult commandResultCpuSet;

    @Mock
    CommandResult commandResultRam;

    @Mock
    CommandResult commandResultDisk;

    @Mock
    CommandResult commandResultCpu;

    @Mock
    ContainerId containerId;


    @Before
    public void setUp() throws Exception
    {
        when( containerId.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( containerId.getHostName() ).thenReturn( CONTAINER_HOST_NAME );
        when( localPeer.getResourceHostByContainerName( anyString() ) ).thenReturn( resourceHost );
        when( localPeer.getResourceHostByContainerId( CONTAINER_HOST_ID ) ).thenReturn( resourceHost );
        //        when( localPeer.getContainerHostById( uuid ) ).thenReturn( containerHost );
        when( containerHost.getHostname() ).thenReturn( containerHostname );

        when( resourceHost.execute( commands.getReadQuotaCommand( containerHostname, ContainerResourceType.RAM ) ) )
                .thenReturn( commandResultRam );

        when( resourceHost.execute( commands.getReadQuotaCommand( containerHostname, ContainerResourceType.CPU ) ) )
                .thenReturn( commandResultCpu );

        when( resourceHost.execute( commands.getReadQuotaCommand( containerHostname, ContainerResourceType.HOME ) ) )
                .thenReturn( commandResultDisk );

        when( resourceHost.execute( commands.getReadQuotaCommand( containerHostname, ContainerResourceType.OPT ) ) )
                .thenReturn( commandResultDisk );

        when( resourceHost.execute( commands.getReadQuotaCommand( containerHostname, ContainerResourceType.VAR ) ) )
                .thenReturn( commandResultDisk );

        when( resourceHost.execute( commands.getReadQuotaCommand( containerHostname, ContainerResourceType.ROOTFS ) ) )
                .thenReturn( commandResultDisk );


        //        when( resourceHost.execute(
        //                commands.getWriteQuotaCommand( containerHostname, ContainerResourceType.RAM, ramQuotaInfo )
        // ) )
        //                .thenReturn( commandResultRam );

//        when( resourceHost
//                .execute( commands.getReadAvailableQuotaCommand( containerHostname, ContainerResourceType.RAM ) ) )
//                .thenReturn( commandResultRam );

        //        when( resourceHost.execute( commands.getWriteRamQuotaCommand2( containerHostname,
        //                String.format( "%s%s", ramQuotaInfo.getRamQuotaValue(),
        //                        ramQuotaInfo.getRamQuotaUnit().getAcronym() ) ) ) ).thenReturn( commandResultRam );

        //        when( resourceHost
        //                .execute( commands.getWriteQuotaCommand( containerHostname, ContainerResourceType.CPU,
        // cpuQuota ) ) )
        //                .thenReturn( commandResultCpu );
//
//        when( resourceHost
//                .execute( commands.getReadAvailableQuotaCommand( containerHostname, ContainerResourceType.CPU ) ) )
//                .thenReturn( commandResultCpu );
//
//        when( resourceHost
//                .execute( commands.getReadAvailableQuotaCommand( containerHostname, ContainerResourceType.HOME ) ) )
//                .thenReturn( commandResultDisk );

        //        when( resourceHost
        //                .execute( commands.getWriteQuotaCommand( containerHostname, ContainerResourceType.HOME,
        // diskQuota ) ) )
        //                .thenReturn( commandResultDisk );

        when( resourceHost.execute( commands.getReadCpuSetCommand( containerHostname ) ) )
                .thenReturn( commandResultCpuSet );

        when( resourceHost.execute( commands.getWriteCpuSetCommand( containerHost.getHostname(), cpuSetString ) ) )
                .thenReturn( commandResultCpuSet );

        when( commandResultCpuSet.hasSucceeded() ).thenReturn( true );
        when( commandResultCpuSet.getStdOut() ).thenReturn( cpuSetCommandOutput );

        when( commandResultRam.hasSucceeded() ).thenReturn( true );
        when( commandResultRam.getStdOut() ).thenReturn( ramQuotaInfo.getWriteValue() );

        when( commandResultCpu.hasSucceeded() ).thenReturn( true );
        when( commandResultCpu.getStdOut() ).thenReturn( cpuQuota.getWriteValue() );

        when( commandResultDisk.hasSucceeded() ).thenReturn( true );
        when( commandResultDisk.getStdOut() ).thenReturn( diskQuota.getWriteValue() );

        quotaManager = new QuotaManagerImpl( peerManager, localPeer, null );
        //        quotaManager.registerResourceValueParsers();
    }


    //    @Test( expected = QuotaException.class )
    //    public void testSetQuota() throws Exception
    //    {
    //        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResultRam );
    //        quotaManager.setQuota( containerId, ContainerResourceType.RAM, ramQuotaValue );
    //
    //        when( commandResultRam.hasSucceeded() ).thenReturn( false );
    //        quotaManager.setQuota( containerId, ContainerResourceType.RAM, new ByteResourceValue( "10", MeasureUnit
    // .GB ) );
    //    }


    //    @Test
    //    public void testGetRamQuota() throws Exception
    //    {
    //        ByteResourceValue result = quotaManager.getQuota( containerId, ContainerResourceType.RAM );
    //        assertEquals( ramQuotaValue.getWriteValue( MeasureUnit.MB ), result.getWriteValue( MeasureUnit.MB ) );
    //    }


    //    @Test( expected = QuotaException.class )
    //    public void testGetAvailableRamQuota() throws Exception
    //    {
    //        assertEquals( ramQuotaValue, quotaManager.getAvailableQuota( containerId, ContainerResourceType.RAM ) );
    //
    //        //For failed command execution test
    //        when( commandResultRam.hasSucceeded() ).thenReturn( false );
    //        quotaManager.getAvailableQuota( containerId, ContainerResourceType.RAM );
    //    }


    //    @Test
    //    public void testGetRamQuotaInfo() throws Exception
    //    {
    //        RamQuota memoryQuotaInfo = quotaManager.getRamQuota( uuid );
    //        assertEquals( ramQuotaInfo.getRamQuotaValue(), ( int ) memoryQuotaInfo.getRamQuotaValue() );
    //    }

    //
    //    @Test
    //    public void testSetRamQuota() throws Exception
    //    {
    //        quotaManager.setQuota( containerId, ramQuotaInfo );
    //        assertEquals( ramQuotaInfo.getValue(),
    //                quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_RAM ).getValue() );
    //    }
    //
    //
    //    @Test
    //    public void testGetCpuQuota() throws Exception
    //    {
    //        quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_CPU );
    //        assertEquals( cpuQuota.getValue(), quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_CPU )
    // .getValue() );
    //    }


    //    @Test
    //    public void testGetCpuQuotaInfo() throws Exception
    //    {
    //        quotaManager.setCpuQuota( uuid, cpuQuota.getPercentage() );
    //        assertEquals( cpuQuota, quotaManager.getCpuQuotaInfo( uuid ) );
    //    }


    //    @Test
    //    public void testSetCpuQuota() throws Exception
    //    {
    //        quotaManager.setQuota( containerId, cpuQuota );
    //        assertEquals( quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_CPU ).getValue(), cpuQuota
    // .getValue() );
    //    }


    @Test
    public void testGetCpuSet() throws Exception
    {
        quotaManager.setCpuSet( containerId, cpuSet );
        assertArrayEquals( cpuSet.toArray(), quotaManager.getCpuSet( containerId ).toArray() );
    }


    @Test
    public void testSetCpuSet() throws Exception
    {
        quotaManager.setCpuSet( containerId, cpuSet );
        assertArrayEquals( cpuSet.toArray(), quotaManager.getCpuSet( containerId ).toArray() );
    }


    //    @Test
    //    public void testGetDiskQuota() throws Exception
    //    {
    //        quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_DISK_OPT );
    //        assertEquals( diskQuota, quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_DISK_HOME ) );
    //    }
    //
    //
    //    @Test
    //    public void testSetDiskQuota() throws Exception
    //    {
    //        quotaManager.setQuota( containerId, diskQuota );
    //        assertEquals( diskQuota, quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_DISK_HOME ) );
    //    }
    //
    //
    //    @Test
    //    public void testSetRamQuota1() throws Exception
    //    {
    //        quotaManager.setRamQuota( uuid, ramQuotaInfo );
    //        assertEquals( ramQuotaInfo.getRamQuotaValue(),
    //                ( int ) quotaManager.getRamQuota( uuid ).getRamQuotaValue() );
    //    }


    //    @Test( expected = QuotaException.class )
    //    public void testGetAvailableCpuQuota() throws Exception
    //    {
    //        assertEquals( cpuQuota.getValue(),
    //                quotaManager.getAvailableQuota( containerId, QuotaType.QUOTA_TYPE_CPU ).getValue() );
    //
    //        //For failed command host not found exception test
    //        when( localPeer.getResourceHostByContainerName( containerHostname ) )
    //                .thenThrow( new HostNotFoundException( "Host Not Found exception test." ) );
    //        quotaManager.getAvailableQuota( containerId, QuotaType.QUOTA_TYPE_CPU );
    //    }
    //


    //    @Test( expected = QuotaException.class )
    //    public void testGetAvailableDiskQuota() throws Exception
    //    {
    //        assertEquals( diskQuota, quotaManager.getAvailableQuota( containerId, QuotaType.QUOTA_TYPE_DISK_HOME ) );
    //
    //        //For resource host not found exception test
    //        when( localPeer.getResourceHostByContainerName( containerHostname ) )
    //                .thenThrow( new HostNotFoundException( "Host Not Found exception test." ) );
    //        quotaManager.getAvailableQuota( containerId, QuotaType.QUOTA_TYPE_DISK_HOME );
    //    }


    //    @Test
    //    public void testGetQuotaInfoCpu() throws Exception
    //    {
    //        assertEquals( cpuQuota, quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_CPU ) );
    //    }
    //
    //
    //    @Test
    //    public void testGetQuotaInfoRootfs() throws Exception
    //    {
    //        DiskQuota diskQuota = new DiskQuota( DiskPartition.ROOT_FS, MeasureUnit.TB, 2 );
    //        assertEquals( diskQuota, quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_DISK_ROOTFS ) );
    //    }
    //
    //
    //    @Test
    //    public void testGetQuotaInfoVar() throws Exception
    //    {
    //        DiskQuota diskQuota = new DiskQuota( DiskPartition.VAR, MeasureUnit.TB, 2 );
    //        assertEquals( diskQuota, quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_DISK_VAR ) );
    //    }


    //    @Test
    //    public void testGetQuotaInfoOpt() throws Exception
    //    {
    //        DiskQuota diskQuota = new DiskQuota( DiskPartition.OPT, MeasureUnit.TB, 2 );
    //        assertEquals( diskQuota, quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_DISK_OPT ) );
    //    }
    //
    //
    //    @Test
    //    public void testGetQuotaInfoHome() throws Exception
    //    {
    //        assertEquals( diskQuota, quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_DISK_HOME ) );
    //    }
    //
    //
    //    @Test
    //    public void testGetQuotaInfoRam() throws Exception
    //    {
    //        quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_RAM );
    //    }
}