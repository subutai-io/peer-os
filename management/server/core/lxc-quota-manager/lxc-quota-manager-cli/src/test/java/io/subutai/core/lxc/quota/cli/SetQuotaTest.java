package io.subutai.core.lxc.quota.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.quota.CpuQuota;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.QuotaType;
import io.subutai.common.quota.RamQuota;
import io.subutai.core.lxc.quota.api.QuotaManager;

import io.subutai.common.peer.LocalPeer;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class SetQuotaTest
{

    SetQuota setQuota;

    private String CONTAINER_HOST_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_HOST_NAME = "containerName";
    private String quotaValue = "50";

    @Mock
    QuotaManager quotaManager;

    @Mock
    PeerManager peerManager;

    @Mock
    LocalPeer localPeer;

    @Mock
    ContainerHost containerHost;

    @Mock
    DiskQuota diskQuota;

    @Mock
    RamQuota ramQuota;

    @Mock
    CpuQuota cpuQuota;

    @Mock
    ContainerId containerId;


    @Before
    public void setUp() throws Exception
    {
        when( containerId.getHostName() ).thenReturn( CONTAINER_HOST_NAME );
        when( containerId.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getContainerHostByName( CONTAINER_HOST_NAME ) ).thenReturn( containerHost );
        when( containerHost.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_RAM ) ).thenReturn( ramQuota );
        when( quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_DISK_HOME ) ).thenReturn( diskQuota );
        when( quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_DISK_OPT ) ).thenReturn( diskQuota );
        when( quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_DISK_ROOTFS ) ).thenReturn( diskQuota );
        when( quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_DISK_VAR ) ).thenReturn( diskQuota );
        when( diskQuota.toString() ).thenReturn( "disk partition" );
        when( quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_CPU ) ).thenReturn( cpuQuota );

        setQuota = new SetQuota( quotaManager, localPeer );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        setQuota.setQuotaType( "ram" );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( CONTAINER_HOST_NAME );
        setQuota.doExecute();

        setQuota.setQuotaType( "cpu" );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( CONTAINER_HOST_NAME );
        setQuota.doExecute();

        setQuota.setQuotaType( "diskHome" );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( CONTAINER_HOST_NAME );
        setQuota.doExecute();

        setQuota.setQuotaType( "diskRootfs" );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( CONTAINER_HOST_NAME );
        setQuota.doExecute();

        setQuota.setQuotaType( "diskVar" );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( CONTAINER_HOST_NAME );
        setQuota.doExecute();

        setQuota.setQuotaType( "diskOpt" );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( CONTAINER_HOST_NAME );
        setQuota.doExecute();
    }
}