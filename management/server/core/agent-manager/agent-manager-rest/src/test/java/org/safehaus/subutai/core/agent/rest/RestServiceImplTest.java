package org.safehaus.subutai.core.agent.rest;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for RestServiceImpl
 */
public class RestServiceImplTest
{

    private AgentManager agentManager;
    private RestServiceImpl restService;
    private static final String HOSTNAME = "hostname";
    private static final String PARENT_NAME = "parent";
    private static final String MAC_ADDRESS = "MAC";
    private static final UUID RANDOM_ID = UUID.randomUUID();
    private static final List<String> IPS = Lists.newArrayList( "127.0.0.1" );


    @Before
    public void setUp()
    {
        agentManager = mock( AgentManager.class );
        restService = new RestServiceImpl( agentManager );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new RestServiceImpl( null );
    }


    @Test
    public void shouldReturnAgents()
    {
        Agent agent = new Agent( RANDOM_ID, HOSTNAME, PARENT_NAME, MAC_ADDRESS, IPS, true, null, RANDOM_ID, RANDOM_ID );
        when( agentManager.getAgents() ).thenReturn( Sets.newHashSet( agent ) );


        Response response = restService.getAgents();


        verify( agentManager ).getAgents();
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( Sets.newHashSet( agent ),
                JsonUtil.fromJson( response.getEntity().toString(), new TypeToken<Set<Agent>>()
                {}.getType() ) );
    }


    @Test
    public void shouldReturnPhysicalAgents()
    {
        Agent agent = new Agent( RANDOM_ID, HOSTNAME, null, MAC_ADDRESS, IPS, false, null, RANDOM_ID, RANDOM_ID );
        when( agentManager.getPhysicalAgents() ).thenReturn( Sets.newHashSet( agent ) );


        Response response = restService.getPhysicalAgents();


        verify( agentManager ).getPhysicalAgents();
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( Sets.newHashSet( agent ),
                JsonUtil.fromJson( response.getEntity().toString(), new TypeToken<Set<Agent>>()
                {}.getType() ) );
    }


    @Test
    public void shouldReturnLxcAgents()
    {
        Agent agent = new Agent( RANDOM_ID, HOSTNAME, PARENT_NAME, MAC_ADDRESS, IPS, true, null, RANDOM_ID, RANDOM_ID );
        when( agentManager.getLxcAgents() ).thenReturn( Sets.newHashSet( agent ) );


        Response response = restService.getLxcAgents();


        verify( agentManager ).getLxcAgents();
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( Sets.newHashSet( agent ),
                JsonUtil.fromJson( response.getEntity().toString(), new TypeToken<Set<Agent>>()
                {}.getType() ) );
    }


    @Test
    public void shouldReturnAgentByHostname()
    {
        Agent agent = new Agent( RANDOM_ID, HOSTNAME, PARENT_NAME, MAC_ADDRESS, IPS, true, null, RANDOM_ID, RANDOM_ID );
        when( agentManager.getAgentByHostname( HOSTNAME ) ).thenReturn( agent );


        Response response = restService.getAgentByHostname( HOSTNAME );


        verify( agentManager ).getAgentByHostname( HOSTNAME );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( agent, JsonUtil.fromJson( response.getEntity().toString(), Agent.class ) );
    }


    @Test
    public void shouldMissAgentByHostname()
    {
        Agent agent = new Agent( RANDOM_ID, HOSTNAME, PARENT_NAME, MAC_ADDRESS, IPS, true, null, RANDOM_ID, RANDOM_ID );


        Response response = restService.getAgentByHostname( HOSTNAME );


        verify( agentManager ).getAgentByHostname( HOSTNAME );
        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnAgentByUUID()
    {
        Agent agent = new Agent( RANDOM_ID, HOSTNAME, PARENT_NAME, MAC_ADDRESS, IPS, true, null, RANDOM_ID, RANDOM_ID );
        when( agentManager.getAgentByUUID( RANDOM_ID ) ).thenReturn( agent );


        Response response = restService.getAgentByUUID( RANDOM_ID.toString() );


        verify( agentManager ).getAgentByUUID( RANDOM_ID );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( agent, JsonUtil.fromJson( response.getEntity().toString(), Agent.class ) );
    }


    @Test
    public void shouldMissAgentByUUID()
    {
        Agent agent = new Agent( RANDOM_ID, HOSTNAME, PARENT_NAME, MAC_ADDRESS, IPS, true, null, RANDOM_ID, RANDOM_ID );


        Response response = restService.getAgentByUUID( RANDOM_ID.toString() );


        verify( agentManager ).getAgentByUUID( RANDOM_ID );
        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldFailByInvalidUUID()
    {


        Response response = restService.getAgentByUUID( null );


        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }
}
