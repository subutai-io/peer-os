package io.subutai.core.lxc.quota.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;
import io.subutai.common.resource.ResourceValueParser;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.peer.api.PeerManager;

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
    ByteValueResource diskQuotaValue;
    @Mock
    ByteValueResource cpuQuotaValue;
    @Mock
    ByteValueResource ramQuotaValue;

    @Mock
    ResourceValueParser resourceValueParser;


    @Before
    public void setUp() throws Exception
    {
        when( containerId.getHostName() ).thenReturn( CONTAINER_HOST_NAME );
        when( containerId.getId() ).thenReturn( CONTAINER_HOST_ID );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getContainerHostById( CONTAINER_HOST_ID ) ).thenReturn( containerHost );
        when( containerHost.getContainerName() ).thenReturn( CONTAINER_HOST_NAME);
//        when( quotaManager.getQuota( containerId, ContainerResourceType.RAM ) ).thenReturn( ramQuotaValue );
//        when( quotaManager.getQuota( containerId, ContainerResourceType.HOME ) ).thenReturn( diskQuotaValue );
//        when( quotaManager.getQuota( containerId, ContainerResourceType.OPT ) ).thenReturn( diskQuotaValue );
//        when( quotaManager.getQuota( containerId, ContainerResourceType.ROOTFS ) ).thenReturn( diskQuotaValue );
//        when( quotaManager.getQuota( containerId, ContainerResourceType.VAR ) ).thenReturn( diskQuotaValue );
//        when( quotaManager.getQuota( containerId, ContainerResourceType.CPU ) ).thenReturn( cpuQuotaValue );
//        when( quotaManager.getResourceValueParser( any( ContainerResourceType.class ) ) ).thenReturn( resourceValueParser );
//        when( resourceValueParser.parse( quotaValue ) ).thenReturn( new ByteResourceValue( quotaValue, MeasureUnit.BYTE ) );

        setQuota = new SetQuota( quotaManager, localPeer );
    }


    @Test
    @Ignore
    public void testDoExecute() throws Exception
    {
        setQuota.setResourceType( ContainerResourceType.RAM.getKey() );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerId( CONTAINER_HOST_ID );
        setQuota.doExecute();

        setQuota.setResourceType( ContainerResourceType.CPU.getKey() );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerId( CONTAINER_HOST_ID );
        setQuota.doExecute();

        setQuota.setResourceType( ContainerResourceType.HOME.getKey() );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerId( CONTAINER_HOST_ID );
        setQuota.doExecute();

        setQuota.setResourceType( ContainerResourceType.ROOTFS.getKey() );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerId( CONTAINER_HOST_ID );
        setQuota.doExecute();

        setQuota.setResourceType( ContainerResourceType.VAR.getKey() );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerId( CONTAINER_HOST_ID );
        setQuota.doExecute();

        setQuota.setResourceType( ContainerResourceType.OPT.getKey() );
        setQuota.setQuotaValue( quotaValue );
        setQuota.setContainerId( CONTAINER_HOST_ID );
        setQuota.doExecute();
    }
}