package io.subutai.core.peer.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.Template;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.peer.api.PeerManager;

import io.subutai.core.registry.api.TemplateRegistry;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ImportTemplateCommandTest extends SystemOutRedirectTest
{
    private static final UUID PEER_ID = UUID.randomUUID();
    @Mock
    PeerManager peerManager;
    @Mock
    TemplateRegistry templateRegistry;
    @Mock
    Template template;
    @Mock
    Peer peer;


    ImportTemplateCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new ImportTemplateCommand();
        command.setPeerManager( peerManager );
        command.setTemplateRegistry( templateRegistry );
        command.peerId = PEER_ID.toString();
        when( templateRegistry.getTemplate( anyString() ) ).thenReturn( template );
        when( peerManager.getPeer( PEER_ID ) ).thenReturn( peer );
        when( peer.getTemplate( anyString() ) ).thenReturn( template );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        assertTrue( getSysOut().contains( "Template already registered" ) );

        when( templateRegistry.getTemplate( anyString() ) ).thenReturn( null );

        command.doExecute();

        assertTrue( getSysOut().contains( "Template successfully obtained" ) );

        when( peer.getTemplate( anyString() ) ).thenReturn( null );

        command.doExecute();

        assertTrue( getSysOut().contains( "Could not obtain template" ) );

        when( templateRegistry.registerTemplate( any( Template.class ) ) ).thenReturn( true );

        command.doExecute();

        assertTrue( getSysOut().contains( "Template registered" ) );
    }
}
