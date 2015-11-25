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
import io.subutai.common.quota.QuotaException;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;
import io.subutai.core.lxc.quota.api.QuotaManager;
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
    ResourceValue diskQuotaValue;
    @Mock
    ResourceValue cpuQuotaValue;
    @Mock
    ResourceValue ramQuotaValue;


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
        when( quotaManager.getQuota( containerId, ResourceType.RAM ) ).thenReturn( ramQuotaValue );
        when( quotaManager.getQuota( containerId, ResourceType.HOME ) ).thenReturn( diskQuotaValue );
        when( quotaManager.getQuota( containerId, ResourceType.OPT ) ).thenReturn( diskQuotaValue );
        when( quotaManager.getQuota( containerId, ResourceType.ROOTFS ) ).thenReturn( diskQuotaValue );
        when( quotaManager.getQuota( containerId, ResourceType.VAR ) ).thenReturn( diskQuotaValue );
        when( quotaManager.getQuota( containerId, ResourceType.CPU ) ).thenReturn( cpuQuotaValue );

        getQuota = new GetQuota( quotaManager, localPeer );
        getQuota.setContainerName( CONTAINER_HOST_NAME );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        getQuota.setResourceType( ResourceType.RAM.name() );
        getQuota.doExecute();

        getQuota.setResourceType( ResourceType.CPU.name() );
        getQuota.doExecute();

        getQuota.setResourceType( ResourceType.HOME.name() );
        getQuota.doExecute();

        getQuota.setResourceType( ResourceType.ROOTFS.name() );
        getQuota.doExecute();

        getQuota.setResourceType( ResourceType.OPT.name() );
        getQuota.doExecute();

        getQuota.setResourceType( ResourceType.VAR.name() );
        getQuota.doExecute();

        //For exception handling purposes
        when( quotaManager.getQuota( containerId, ResourceType.CPU ) )
                .thenThrow( new QuotaException( "Some quota exception" ) );
        getQuota.setResourceType( ResourceType.CPU.name() );
        getQuota.doExecute();
    }
}