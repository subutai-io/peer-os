package org.safehaus.subutai.core.agent.cli;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;

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
public class GetAgentByUUIDCommandTest
{
    private ByteArrayOutputStream myOut;
    private static final UUID AGENT_ID = UUID.randomUUID();
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
        new GetAgentByUUIDCommand( null );
    }


    @Test
    public void shouldReturnAgent()
    {
        AgentManager agentManager = mock( AgentManager.class );
        Agent agent = mock( Agent.class );
        when( agent.toString() ).thenReturn( AGENT_TO_STRING );
        when( agentManager.getAgentByUUID( AGENT_ID ) ).thenReturn( agent );

        GetAgentByUUIDCommand getAgentByUUIDCommand = new GetAgentByUUIDCommand( agentManager );
        getAgentByUUIDCommand.setUuid( AGENT_ID.toString() );
        getAgentByUUIDCommand.doExecute();

        assertEquals( AGENT_TO_STRING, getSysOut() );
    }


    @Test
    public void shouldMissAgent()
    {
        AgentManager agentManager = mock( AgentManager.class );

        GetAgentByUUIDCommand getAgentByUUIDCommand = new GetAgentByUUIDCommand( agentManager );
        getAgentByUUIDCommand.setUuid( UUID.randomUUID().toString() );
        getAgentByUUIDCommand.doExecute();

        assertEquals( AGENT_NOT_FOUND_MSG, getSysOut() );
    }
}
