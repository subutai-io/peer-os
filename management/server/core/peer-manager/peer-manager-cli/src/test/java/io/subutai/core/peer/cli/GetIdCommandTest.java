package io.subutai.core.peer.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.peer.api.PeerManager;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class GetIdCommandTest extends SystemOutRedirectTest
{
    private static final String ID = UUID.randomUUID().toString();
    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;

    GetIdCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new GetIdCommand();
        command.setPeerManager( peerManager );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getId() ).thenReturn( ID );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        assertTrue( getSysOut().contains( ID.toString() ) );
    }
}
