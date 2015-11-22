package io.subutai.core.lxc.quota.impl;


import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.quota.CpuQuota;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.DiskQuotaUnit;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.quota.QuotaType;
import io.subutai.common.quota.RamQuota;
import io.subutai.common.quota.RamQuotaUnit;
import io.subutai.core.lxc.quota.impl.parser.CpuQuotaParser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class QuotaManagerImplTest
{


    private QuotaManagerImpl quotaManager;
    private Commands commands = new Commands();

    private String uuid = UUID.randomUUID().toString();
    private String containerName = "containerName";
    private String containerHostname = "containerHostname";
    private String quotaResult = "100";
    private String cpuSetCommandOutput = "1-5,7";
    private String cpuSetString = "1,2,3,4,5,7";

    private Set<Integer> cpuSet = Sets.newHashSet( 1, 2, 3, 4, 5, 7 );

    private RamQuota ramQuotaInfo = new RamQuota( RamQuotaUnit.MB, 16 );
    private CpuQuota cpuQuota = CpuQuotaParser.getInstance().parse( quotaResult );

    private DiskQuota diskQuota = new DiskQuota( DiskPartition.HOME, DiskQuotaUnit.TB, 2 );

    @Mock
    ResourceHost resourceHost;

    @Mock
    LocalPeer localPeer;

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


    @Before
    public void setUp() throws Exception
    {
        when( localPeer.getResourceHostByContainerName( anyString() ) ).thenReturn( resourceHost );
        when( localPeer.getResourceHostByContainerId( uuid ) ).thenReturn( resourceHost );
        when( localPeer.getContainerHostById( uuid ) ).thenReturn( containerHost );
        when( containerHost.getHostname() ).thenReturn( containerHostname );

        when( resourceHost.execute( commands.getReadQuotaCommand( containerHostname, QuotaType.QUOTA_TYPE_RAM ) ) )
                .thenReturn( commandResultRam );

        when( resourceHost.execute( commands.getReadQuotaCommand( containerHostname, QuotaType.QUOTA_TYPE_CPU ) ) )
                .thenReturn( commandResultCpu );

        when( resourceHost
                .execute( commands.getReadQuotaCommand( containerHostname, QuotaType.QUOTA_TYPE_DISK_HOME ) ) )
                .thenReturn( commandResultDisk );

        when( resourceHost.execute( commands.getReadQuotaCommand( containerHostname, QuotaType.QUOTA_TYPE_DISK_OPT ) ) )
                .thenReturn( commandResultDisk );

        when( resourceHost.execute( commands.getReadQuotaCommand( containerHostname, QuotaType.QUOTA_TYPE_DISK_VAR ) ) )
                .thenReturn( commandResultDisk );

        when( resourceHost.execute( commands.getReadQuotaCommand( containerHostname, QuotaType.QUOTA_TYPE_DISK_ROOTFS ) ) )
                .thenReturn( commandResultDisk );


        when( resourceHost
                .execute( commands.getWriteRamQuotaCommand( containerHostname, ramQuotaInfo.getRamQuotaValue() ) ) )
                .thenReturn( commandResultRam );

        when( resourceHost.execute( commands.getReadAvailableRamQuotaCommand( containerHostname ) ) )
                .thenReturn( commandResultRam );

        when( resourceHost.execute( commands.getWriteRamQuotaCommand2( containerHostname,
                String.format( "%s%s", ramQuotaInfo.getRamQuotaValue(),
                        ramQuotaInfo.getRamQuotaUnit().getAcronym() ) ) ) ).thenReturn( commandResultRam );

        when( resourceHost.execute( commands.getWriteCpuQuotaCommand( containerHostname, cpuQuota.getPercentage() ) ) )
                .thenReturn( commandResultCpu );

        when( resourceHost.execute( commands.getReadAvailableCpuQuotaCommand( containerHostname ) ) )
                .thenReturn( commandResultCpu );

        when( resourceHost.execute( commands.getReadAvailableDiskQuotaCommand( containerHostname,
                DiskPartition.HOME.getPartitionName() ) ) ).thenReturn( commandResultDisk );

        when( resourceHost.execute(
                commands.getWriteDiskQuotaCommand( containerHostname, diskQuota.getDiskPartition().getPartitionName(),
                        String.format( "%s%s", diskQuota.getDiskQuotaValue(),
                                diskQuota.getDiskQuotaUnit().getAcronym() ) ) ) ).thenReturn( commandResultDisk );

        when( resourceHost.execute( commands.getReadCpuSetCommand( containerHostname ) ) )
                .thenReturn( commandResultCpuSet );

        when( resourceHost.execute( commands.getWriteCpuSetCommand( containerHost.getHostname(), cpuSetString ) ) )
                .thenReturn( commandResultCpuSet );

        when( commandResultCpuSet.hasSucceeded() ).thenReturn( true );
        when( commandResultCpuSet.getStdOut() ).thenReturn( cpuSetCommandOutput );

        when( commandResultRam.hasSucceeded() ).thenReturn( true );
        when( commandResultRam.getStdOut() ).thenReturn( ramQuotaInfo.getValue() );

        when( commandResultCpu.hasSucceeded() ).thenReturn( true );
        when( commandResultCpu.getStdOut() ).thenReturn( cpuQuota.getValue() );

        when( commandResultDisk.hasSucceeded() ).thenReturn( true );
        when( commandResultDisk.getStdOut() ).thenReturn( diskQuota.getValue() );

        quotaManager = new QuotaManagerImpl( localPeer );
        quotaManager.registerQuotaParsers();
    }


    @Test( expected = QuotaException.class )
    public void testSetQuota() throws Exception
    {
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResultRam );
        quotaManager.setQuota( containerName, ramQuotaInfo );

        when( commandResultRam.hasSucceeded() ).thenReturn( false );
        quotaManager.setQuota( containerName, new RamQuota( RamQuotaUnit.GB, 10 ) );
    }


    @Test
    public void testGetRamQuota() throws Exception
    {
        int result = quotaManager.getRamQuota( uuid );
        assertEquals( ramQuotaInfo.getRamQuotaValue(), result );
    }


    @Test
    public void testGetRamQuotaInfo() throws Exception
    {
        RamQuota memoryQuotaInfo = quotaManager.getRamQuotaInfo( uuid );
        assertEquals( ramQuotaInfo.getRamQuotaValue(), ( int ) memoryQuotaInfo.getRamQuotaValue() );
    }


    @Test
    public void testSetGetRamQuota() throws Exception
    {
        quotaManager.setRamQuota( uuid, ramQuotaInfo.getRamQuotaValue() );
        assertEquals( ramQuotaInfo.getRamQuotaValue(), quotaManager.getRamQuota( uuid ) );
    }


    @Test
    public void testGetCpuQuota() throws Exception
    {
        quotaManager.setCpuQuota( uuid, cpuQuota.getPercentage() );
        assertEquals( cpuQuota.getPercentage(), quotaManager.getCpuQuota( uuid ) );
    }


    @Test
    public void testGetCpuQuotaInfo() throws Exception
    {
        quotaManager.setCpuQuota( uuid, cpuQuota.getPercentage() );
        assertEquals( cpuQuota, quotaManager.getCpuQuotaInfo( uuid ) );
    }


    @Test
    public void testSetCpuQuota() throws Exception
    {
        quotaManager.setCpuQuota( uuid, cpuQuota.getPercentage() );
        assertEquals( quotaManager.getCpuQuota( uuid ), cpuQuota.getPercentage() );
    }


    @Test
    public void testGetCpuSet() throws Exception
    {
        quotaManager.setCpuSet( uuid, cpuSet );
        assertArrayEquals( cpuSet.toArray(), quotaManager.getCpuSet( uuid ).toArray() );
    }


    @Test
    public void testSetCpuSet() throws Exception
    {
        quotaManager.setCpuSet( uuid, cpuSet );
        assertArrayEquals( cpuSet.toArray(), quotaManager.getCpuSet( uuid ).toArray() );
    }


    @Test
    public void testGetDiskQuota() throws Exception
    {
        quotaManager.setDiskQuota( uuid, diskQuota );
        assertEquals( diskQuota, quotaManager.getDiskQuota( uuid, DiskPartition.HOME ) );
    }


    @Test
    public void testSetDiskQuota() throws Exception
    {
        quotaManager.setDiskQuota( uuid, diskQuota );
        assertEquals( diskQuota, quotaManager.getDiskQuota( uuid, DiskPartition.HOME ) );
    }


    @Test
    public void testSetRamQuota1() throws Exception
    {
        quotaManager.setRamQuota( uuid, ramQuotaInfo );
        assertEquals( ramQuotaInfo.getRamQuotaValue(),
                ( int ) quotaManager.getRamQuotaInfo( uuid ).getRamQuotaValue() );
    }


    @Test( expected = QuotaException.class )
    public void testGetAvailableRamQuota() throws Exception
    {
        assertEquals( ramQuotaInfo.getRamQuotaValue(), quotaManager.getAvailableRamQuota( uuid ) );

        //For failed command execution test
        when( commandResultRam.hasSucceeded() ).thenReturn( false );
        quotaManager.getAvailableRamQuota( uuid );
    }


    @Test( expected = QuotaException.class )
    public void testGetAvailableCpuQuota() throws Exception
    {
        assertEquals( cpuQuota.getPercentage(), quotaManager.getAvailableCpuQuota( uuid ) );

        //For failed command host not found exception test
        when( localPeer.getContainerHostById( uuid ) )
                .thenThrow( new HostNotFoundException( "Host Not Found exception test." ) );
        quotaManager.getAvailableRamQuota( uuid );
    }


    @Test( expected = QuotaException.class )
    public void testGetAvailableDiskQuota() throws Exception
    {
        assertEquals( diskQuota, quotaManager.getAvailableDiskQuota( uuid, DiskPartition.HOME ) );

        //For resource host not found exception test
        when( localPeer.getResourceHostByContainerId( uuid ) )
                .thenThrow( new HostNotFoundException( "Host Not Found exception test." ) );
        quotaManager.getAvailableRamQuota( uuid );
    }


    @Test
    public void testGetQuotaInfoCpu() throws Exception
    {
        assertEquals( cpuQuota, quotaManager.getQuota( containerHostname, QuotaType.QUOTA_TYPE_CPU ) );
    }


    @Test
    public void testGetQuotaInfoRootfs() throws Exception
    {
        DiskQuota diskQuota = new DiskQuota( DiskPartition.ROOT_FS, DiskQuotaUnit.TB, 2 );
        assertEquals( diskQuota, quotaManager.getQuota( containerHostname, QuotaType.QUOTA_TYPE_DISK_ROOTFS ) );
    }


    @Test
    public void testGetQuotaInfoVar() throws Exception
    {
        DiskQuota diskQuota = new DiskQuota( DiskPartition.VAR, DiskQuotaUnit.TB, 2 );
        assertEquals( diskQuota, quotaManager.getQuota( containerHostname, QuotaType.QUOTA_TYPE_DISK_VAR ) );
    }


    @Test
    public void testGetQuotaInfoOpt() throws Exception
    {
        DiskQuota diskQuota = new DiskQuota( DiskPartition.OPT, DiskQuotaUnit.TB, 2 );
        assertEquals( diskQuota, quotaManager.getQuota( containerHostname, QuotaType.QUOTA_TYPE_DISK_OPT ) );
    }


    @Test
    public void testGetQuotaInfoHome() throws Exception
    {
        assertEquals( diskQuota, quotaManager.getQuota( containerHostname, QuotaType.QUOTA_TYPE_DISK_HOME ) );
    }


    @Test
    public void testGetQuotaInfoRam() throws Exception
    {


        quotaManager.getQuota( containerHostname, QuotaType.QUOTA_TYPE_RAM );
    }
}