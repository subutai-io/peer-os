package io.subutai.core.environment.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.environment.api.EnvironmentManager;

import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;

import com.google.common.collect.Sets;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class GrowLocalEnvironmentCommandTest extends SystemOutRedirectTest
{

    @Mock
    EnvironmentManager environmentManager;
    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    ContainerHost containerHost;

    GrowLocalEnvironmentCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new GrowLocalEnvironmentCommand( environmentManager, peerManager );
        command.async = TestUtil.ASYNC;
        command.environmentId = TestUtil.ENV_ID.toString();
        command.numberOfContainers = TestUtil.NUMBER_OF_CONTAINERS;
        command.templateName = TestUtil.TEMPLATE_NAME;
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( environmentManager.growEnvironment( any( UUID.class ), any( Topology.class ), anyBoolean() ) )
                .thenReturn( Sets.newHashSet( containerHost ) );
        when( containerHost.getId() ).thenReturn( TestUtil.CONTAINER_ID );
        when( containerHost.getHostname() ).thenReturn( TestUtil.HOSTNAME );
        when( containerHost.getEnvironmentId() ).thenReturn( TestUtil.ENV_ID.toString() );
        when( containerHost.getTemplateName() ).thenReturn( TestUtil.TEMPLATE_NAME );
        when( containerHost.getNodeGroupName() ).thenReturn( TestUtil.NODE_GROUP_NAME );
        when( containerHost.isConnected() ).thenReturn( TestUtil.IS_CONNECTED );
        when( containerHost.getIpByInterfaceName( anyString() ) ).thenReturn( TestUtil.IP );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        assertThat( getSysOut(), containsString( "New containers created:" ) );
        assertThat( getSysOut(), containsString( TestUtil.HOSTNAME ) );
    }
}
