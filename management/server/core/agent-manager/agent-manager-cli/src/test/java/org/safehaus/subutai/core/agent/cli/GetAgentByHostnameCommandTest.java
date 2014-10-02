package org.safehaus.subutai.core.agent.cli;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for GetAgentByHostnameCommand
 */
public class GetAgentByHostnameCommandTest
{
    private ByteArrayOutputStream myOut;
    private static final String HOSTNAME = "hostname";
    private static final String AGENT_TO_STRING = "agent";
    private static final String AGENT_NOT_FOUND_MSG = "Agent not found";


    @Before
    public void setUp()
    {
        myOut = new ByteArrayOutputStream();
        System.setOut( new PrintStream( myOut ) );
    }


    @After
    public void tearDown()
    {
        System.setOut( System.out );
    }


    private String getSysOut()
    {
        return myOut.toString().trim();
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new GetAgentByHostnameCommand( null );
    }


    @Test
    public void shouldReturnAgent()
    {
        AgentManager agentManager = mock( AgentManager.class );
        Agent agent = mock( Agent.class );
        when( agent.toString() ).thenReturn( AGENT_TO_STRING );
        when( agentManager.getAgentByHostname( HOSTNAME ) ).thenReturn( agent );

        GetAgentByHostnameCommand getAgentByHostnameCommand = new GetAgentByHostnameCommand( agentManager );
        getAgentByHostnameCommand.setHostname( HOSTNAME );
        getAgentByHostnameCommand.doExecute();

        assertEquals( AGENT_TO_STRING, getSysOut() );
    }


    @Test
    public void shouldMissAgent()
    {
        AgentManager agentManager = mock( AgentManager.class );

        GetAgentByHostnameCommand getAgentByHostnameCommand = new GetAgentByHostnameCommand( agentManager );
        getAgentByHostnameCommand.doExecute();

        assertEquals( AGENT_NOT_FOUND_MSG, getSysOut() );
    }
}
