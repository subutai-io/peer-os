package io.subutai.core.environment.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Topology;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.environment.api.EnvironmentManager;

import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class BuildLocalEnvironmentCommandTest extends SystemOutRedirectTest
{


    @Mock
    EnvironmentManager environmentManager;
    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    Environment environment;

    private BuildLocalEnvironmentCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new BuildLocalEnvironmentCommand( environmentManager, peerManager );
        command.templateName = TestUtil.TEMPLATE_NAME;
        command.numberOfContainers = TestUtil.NUMBER_OF_CONTAINERS;
        command.subnetCidr = TestUtil.SUBNET;
        command.async = TestUtil.ASYNC;
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( environmentManager
                .createEnvironment( anyString(), any( Topology.class ), anyString(), anyString(), anyBoolean() ) )
                .thenReturn( environment );
        when( environment.getId() ).thenReturn( TestUtil.ENV_ID );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        assertThat( getSysOut(), containsString( TestUtil.ENV_ID.toString() ) );
    }
}
