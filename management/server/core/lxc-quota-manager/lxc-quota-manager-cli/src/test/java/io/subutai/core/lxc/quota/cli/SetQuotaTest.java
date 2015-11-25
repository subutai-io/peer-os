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
import io.subutai.common.resource.MeasureUnit;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;
import io.subutai.common.resource.ResourceValueParser;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.any;
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
    ContainerId containerId;


    @Mock
    ResourceValue diskQuotaValue;
    @Mock
    ResourceValue cpuQuotaValue;
    @Mock
    ResourceValue ramQuotaValue;

    @Mock
    ResourceValueParser resourceValueParser;


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
        when( quotaManager.getResourceValueParser( any( ResourceType.class ) ) ).thenReturn( resourceValueParser );
        when( resourceValueParser.parse( quotaValue ) ).thenReturn( new ResourceValue( quotaValue, MeasureUnit.BYTE ) );

        setQuota = new SetQuota( quotaManager, localPeer );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        setQuota.setResourceType( ResourceType.RAM.name() );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( CONTAINER_HOST_NAME );
        setQuota.doExecute();

        setQuota.setResourceType( ResourceType.CPU.name() );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( CONTAINER_HOST_NAME );
        setQuota.doExecute();

        setQuota.setResourceType( ResourceType.HOME.name() );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( CONTAINER_HOST_NAME );
        setQuota.doExecute();

        setQuota.setResourceType( ResourceType.ROOTFS.name() );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( CONTAINER_HOST_NAME );
        setQuota.doExecute();

        setQuota.setResourceType( ResourceType.VAR.name() );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( CONTAINER_HOST_NAME );
        setQuota.doExecute();

        setQuota.setResourceType( ResourceType.OPT.name() );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerName( CONTAINER_HOST_NAME );
        setQuota.doExecute();
    }
}