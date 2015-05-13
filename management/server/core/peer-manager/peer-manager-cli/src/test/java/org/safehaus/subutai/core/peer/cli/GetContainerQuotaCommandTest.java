package org.safehaus.subutai.core.peer.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.peer.api.PeerManager;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class GetContainerQuotaCommandTest extends SystemOutRedirectTest
{
    private static final String CONTAINER_NAME = "container";
    @Mock
    PeerManager peerManager;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    Peer peer;
    @Mock
    Environment environment;
    @Mock
    ContainerHost containerHost;

    GetContainerQuotaCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new GetContainerQuotaCommand( peerManager, environmentManager );
        when( peerManager.getPeer( anyString() ) ).thenReturn( peer );
        when( environmentManager.findEnvironment( any( UUID.class ) ) ).thenReturn( environment );
        when( environment.getContainerHostByHostname( anyString() ) ).thenReturn( containerHost );
        command.environmentId = UUID.randomUUID().toString();
        command.quotaType = QuotaType.QUOTA_TYPE_CPU.getKey();
        command.containerName = CONTAINER_NAME;
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        verify( peer ).getQuotaInfo( containerHost, QuotaType.QUOTA_TYPE_CPU );

        when( environment.getContainerHostByHostname( anyString() ) ).thenReturn( null );

        command.doExecute();

        assertTrue( getSysOut().contains( CONTAINER_NAME ) );
    }
}
