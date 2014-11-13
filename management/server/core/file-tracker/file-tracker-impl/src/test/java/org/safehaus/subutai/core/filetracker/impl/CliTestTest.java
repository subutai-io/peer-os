package org.safehaus.subutai.core.filetracker.impl;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.filetracker.api.FileTracker;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;

import java.util.UUID;

import static org.mockito.Mockito.*;


/**
 * Created by talas on 10/3/14.
 */
public class CliTestTest
{
    private PeerManager peerManager;
    private CliTest cliTest;
    private FileTracker fileTracker;

    @Before
    public void setupClasses()
    {
        peerManager = mock( PeerManager.class );
        fileTracker  = mock( FileTracker.class );
        cliTest = new CliTest();
        cliTest.setFileTracker( fileTracker );
        cliTest.setPeerManager(peerManager);
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowNullPointerExceptionOnSetPeerManager()
    {
        cliTest.setPeerManager( null );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowNullPointerExceptionOnSetFileTracker()
    {
        cliTest.setFileTracker( null );
    }


    //    @Test
    //    public void shouldAccessFileTrackerAndAgentManagerOnDoExecute()
    //    {
    //        when( agentManager.getAgents() ).thenReturn( Collections.<Agent>emptySet() );
    //        cliTest.doExecute();
    //        verify( agentManager ).getAgents();
    //    }


    @Test
    public void justDummyMethodOnResponse()
    {
        Response response = mock( Response.class );
        cliTest.onResponse( response );
    }

    @Test
    public void testDoExecute() throws Exception {

        String[] CONFIG_POINTS = new String[] { "/etc", "/etc/subutai-agent" };

        ManagementHost managementHost = new ManagementHost(mock(Agent.class), UUID.randomUUID());

        LocalPeer localPeer = mock(LocalPeer.class);
        when(peerManager.getLocalPeer()).thenReturn(localPeer);

        when(localPeer.getManagementHost()).thenReturn(managementHost);

        cliTest.doExecute();


        verify(fileTracker).createConfigPoints(managementHost,CONFIG_POINTS);
        verify(peerManager).getLocalPeer();
        verify(peerManager.getLocalPeer()).getManagementHost();
    }
}
