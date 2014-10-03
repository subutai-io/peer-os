package org.safehaus.subutai.core.agent.cli;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for GetLxcAgentsCommand
 */
public class GetLxcAgentsCommandTest
{
    private ByteArrayOutputStream myOut;
    private static final String HOSTNAME = "hostname";


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
        new GetLxcAgentsCommand( null );
    }


    @Test
    public void shouldReturnAgents()
    {
        AgentManager agentManager = mock( AgentManager.class );
        Agent agent = mock( Agent.class );
        when( agent.getHostname() ).thenReturn( HOSTNAME );
        when( agentManager.getLxcAgents() ).thenReturn( Sets.newHashSet( agent ) );

        GetLxcAgentsCommand getLxcAgentsCommand = new GetLxcAgentsCommand( agentManager );
        getLxcAgentsCommand.doExecute();

        assertEquals( HOSTNAME, getSysOut() );
    }
}
