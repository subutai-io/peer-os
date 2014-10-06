package org.safehaus.subutai.core.filetracker.impl;


import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.filetracker.api.FileTracker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by talas on 10/3/14.
 */
public class CliTestTest
{
    private AgentManager agentManager;
    private CliTest cliTest;


    @Before
    public void setupClasses()
    {
        agentManager = mock( AgentManager.class );
        final FileTracker fileTracker = mock( FileTracker.class );
        cliTest = new CliTest();
        cliTest.setAgentManager( agentManager );
        cliTest.setFileTracker( fileTracker );
    }


    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionOnSetAgentManager()
    {
        cliTest.setAgentManager( null );
    }


    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionOnSetFileTracker()
    {
        cliTest.setFileTracker( null );
    }


    @Test
    public void shouldAccessFileTrackerAndAgentManagerOnDoExecute()
    {
        when( agentManager.getAgents() ).thenReturn( Collections.<Agent>emptySet() );
        cliTest.doExecute();
        verify( agentManager ).getAgents();
    }


    @Test
    public void justDummyMethodOnResponse()
    {
        Response response = mock( Response.class );
        cliTest.onResponse( response );
    }
}
