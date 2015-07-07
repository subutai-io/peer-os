package io.subutai.core.lxc.quota.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.QuotaException;
import io.subutai.core.lxc.quota.api.QuotaManager;

import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class GetQuotaTest
{
    private String containerName = "containerName";
    private GetQuota getQuota;
    private UUID uuid = UUID.randomUUID();

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

        getQuota = new GetQuota( quotaManager, peerManager );
        getQuota.setContainerName( containerName );
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
        when( quotaManager.getCpuQuota( uuid ) ).thenThrow( new QuotaException( "Some quota exception" ) );
        getQuota.setQuotaType( "cpu" );
        getQuota.doExecute();
    }
}