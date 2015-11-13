package io.subutai.core.lxc.quota.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.core.lxc.quota.api.QuotaManager;

import io.subutai.common.peer.LocalPeer;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class SetQuotaTest
{

    SetQuota setQuota;

    private String containerName = "containerName";
    private String quotaValue = "50";
    private String uuid = UUID.randomUUID().toString();

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


    @Before
    public void setUp() throws Exception
    {
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getContainerHostByName( containerName ) ).thenReturn( containerHost );
        when( containerHost.getId() ).thenReturn( uuid );
        when( quotaManager.getRamQuota( uuid ) ).thenReturn( 100 );
        when( quotaManager.getDiskQuota( uuid, DiskPartition.HOME ) ).thenReturn( diskQuota );
        when( quotaManager.getDiskQuota( uuid, DiskPartition.OPT ) ).thenReturn( diskQuota );
        when( quotaManager.getDiskQuota( uuid, DiskPartition.ROOT_FS ) ).thenReturn( diskQuota );
        when( quotaManager.getDiskQuota( uuid, DiskPartition.VAR ) ).thenReturn( diskQuota );
        when( diskQuota.toString() ).thenReturn( "disk partition" );
        when( quotaManager.getCpuQuota( uuid ) ).thenReturn( 100 );
        setQuota = new SetQuota( quotaManager, peerManager );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        setQuota.setQuotaType( "ram" );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( containerName );
        setQuota.doExecute();

        setQuota.setQuotaType( "cpu" );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( containerName );
        setQuota.doExecute();

        setQuota.setQuotaType( "diskHome" );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( containerName );
        setQuota.doExecute();

        setQuota.setQuotaType( "diskRootfs" );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( containerName );
        setQuota.doExecute();

        setQuota.setQuotaType( "diskVar" );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( containerName );
        setQuota.doExecute();

        setQuota.setQuotaType( "diskOpt" );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( containerName );
        setQuota.doExecute();
    }
}