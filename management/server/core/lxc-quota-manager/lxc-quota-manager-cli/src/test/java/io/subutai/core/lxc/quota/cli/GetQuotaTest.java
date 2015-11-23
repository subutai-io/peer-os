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
import io.subutai.common.quota.QuotaException;
import io.subutai.common.quota.QuotaType;
import io.subutai.common.quota.RamQuota;
import io.subutai.core.lxc.quota.api.QuotaManager;

import io.subutai.common.peer.LocalPeer;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class GetQuotaTest
{
    private static final String CONTAINER_HOST_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_HOST_NAME = "containerName";
    private GetQuota getQuota;

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
        when( containerId.getId()).thenReturn( CONTAINER_HOST_ID );
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

        getQuota = new GetQuota( quotaManager, localPeer );
        getQuota.setContainerName( CONTAINER_HOST_NAME );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        getQuota.setQuotaType( "ram" );
        getQuota.doExecute();

        getQuota.setQuotaType( "cpu" );
        getQuota.doExecute();

        getQuota.setQuotaType( "diskHome" );
        getQuota.doExecute();

        getQuota.setQuotaType( "diskRootfs" );
        getQuota.doExecute();

        getQuota.setQuotaType( "diskOpt" );
        getQuota.doExecute();

        getQuota.setQuotaType( "diskVar" );
        getQuota.doExecute();

        //For exception handling purposes
        when( quotaManager.getQuota( containerId, QuotaType.QUOTA_TYPE_CPU ) ).thenThrow( new QuotaException( "Some quota exception" ) );
        getQuota.setQuotaType( "cpu" );
        getQuota.doExecute();
    }
}