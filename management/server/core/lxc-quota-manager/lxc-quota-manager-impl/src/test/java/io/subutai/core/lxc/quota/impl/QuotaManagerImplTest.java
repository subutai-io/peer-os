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
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.parser.CpuResourceValueParser;
import io.subutai.hub.share.parser.DiskValueResourceParser;
import io.subutai.hub.share.quota.ContainerCpuResource;
import io.subutai.hub.share.quota.ContainerHomeResource;
import io.subutai.hub.share.quota.ContainerRamResource;
import io.subutai.hub.share.resource.ByteUnit;
import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;

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
        when( localPeer.getResourceHostByContainerHostName( anyString() ) ).thenReturn( resourceHost );
        when( localPeer.getResourceHostByContainerId( CONTAINER_HOST_ID ) ).thenReturn( resourceHost );
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

        quotaManager = new QuotaManagerImpl( peerManager, localPeer );
    }


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
}