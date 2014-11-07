package org.safehaus.subutai.core.filetracker.impl;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.filetracker.api.FileTracker;
import org.safehaus.subutai.core.peer.api.PeerManager;

import static org.mockito.Mockito.mock;


/**
 * Created by talas on 10/3/14.
 */
public class CliTestTest
{
    private PeerManager peerManager;
    private CliTest cliTest;


    @Before
    public void setupClasses()
    {
        peerManager = mock( PeerManager.class );
        final FileTracker fileTracker = mock( FileTracker.class );
        cliTest = new CliTest();
        cliTest.setFileTracker( fileTracker );
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
}
