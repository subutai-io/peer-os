package io.subutai.core.lxc.quota.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.quota.QuotaException;
import io.subutai.hub.share.resource.ByteValueResource;

import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class GetQuotaTest
{
    private static final String CONTAINER_HOST_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_NAME = "containerName";
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
    ByteValueResource diskQuotaValue;
    @Mock
    ByteValueResource cpuQuotaValue;
    @Mock
    ByteValueResource ramQuotaValue;


    @Mock
    ContainerId containerId;


    @Before
    public void setUp() throws Exception
    {
        when( containerId.getHostName() ).thenReturn( CONTAINER_NAME );
        when( containerId.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getContainerHostByContainerName( CONTAINER_NAME ) ).thenReturn( containerHost );
        when( containerHost.getId() ).thenReturn( CONTAINER_HOST_ID );
        //        when( quotaManager.getQuota( containerId, ContainerResourceType.RAM ) ).thenReturn( ramQuotaValue );
        //        when( quotaManager.getQuota( containerId, ContainerResourceType.HOME ) ).thenReturn( diskQuotaValue );
        //        when( quotaManager.getQuota( containerId, ContainerResourceType.OPT ) ).thenReturn( diskQuotaValue );
        //        when( quotaManager.getQuota( containerId, ContainerResourceType.ROOTFS ) ).thenReturn(
        // diskQuotaValue );
        //        when( quotaManager.getQuota( containerId, ContainerResourceType.VAR ) ).thenReturn( diskQuotaValue );
        //        when( quotaManager.getQuota( containerId, ContainerResourceType.CPU ) ).thenReturn( cpuQuotaValue );

        getQuota = new GetQuota( quotaManager, localPeer );
        getQuota.setContainerName( CONTAINER_NAME );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        getQuota.doExecute();

        //For exception handling purposes
        when( quotaManager.getQuota( containerId ) ).thenThrow( new QuotaException( "Some quota exception" ) );
        getQuota.doExecute();
    }
}