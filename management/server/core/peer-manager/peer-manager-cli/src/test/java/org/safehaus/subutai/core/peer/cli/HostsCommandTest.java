package org.safehaus.subutai.core.peer.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.User;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class HostsCommandTest extends SystemOutRedirectTest
{
    private static final String CONTAINER_HOST_HOSTNAME = "container";
    private static final String RESOURCE_HOST_HOSTNAME = "resource";
    private static final String MGMT_HOST_HOSTNAME = "management";
    private static final UUID ID = UUID.randomUUID();
    private static final String USERNAME = "user";
    private static final long LAST_HEARTBEAT = System.currentTimeMillis();
    @Mock
    PeerManager peerManager;
    @Mock
    IdentityManager identityManager;
    @Mock
    User user;
    @Mock
    LocalPeer localPeer;
    @Mock
    ManagementHost managementHost;
    @Mock
    ResourceHost resourceHost;
    @Mock
    ContainerHost containerHost;

    HostsCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new HostsCommand();
        command.setPeerManager( peerManager );
        command.setIdentityManager( identityManager );
        when( identityManager.getUser() ).thenReturn( user );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( localPeer.getResourceHosts() ).thenReturn( Sets.newHashSet( resourceHost ) );
        when( resourceHost.getContainerHosts() ).thenReturn( Sets.newHashSet( containerHost ) );
        when( managementHost.getHostname() ).thenReturn( MGMT_HOST_HOSTNAME );
        when( resourceHost.getHostname() ).thenReturn( RESOURCE_HOST_HOSTNAME );
        when( containerHost.getHostname() ).thenReturn( CONTAINER_HOST_HOSTNAME );
        when( user.getUsername() ).thenReturn( USERNAME );
        when( managementHost.getLastHeartbeat() ).thenReturn( LAST_HEARTBEAT );
        when( resourceHost.getLastHeartbeat() ).thenReturn( LAST_HEARTBEAT );
        when( containerHost.getLastHeartbeat() ).thenReturn( LAST_HEARTBEAT );
        when( managementHost.getId() ).thenReturn( ID );
        when( resourceHost.getId() ).thenReturn( ID );
        when( containerHost.getId() ).thenReturn( ID );
        when( containerHost.getState() ).thenReturn( ContainerHostState.RUNNING );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        assertTrue( getSysOut().contains( MGMT_HOST_HOSTNAME ) );
        assertTrue( getSysOut().contains( RESOURCE_HOST_HOSTNAME ) );
        assertTrue( getSysOut().contains( CONTAINER_HOST_HOSTNAME ) );
    }
}
