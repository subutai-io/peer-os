package org.safehaus.subutai.core.agent.rest;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
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
    private static final UUID RANDOM_ID = UUIDUtil.generateTimeBasedUUID();
    ;
    private static final List<String> IPS = Lists.newArrayList( "127.0.0.1" );

    private Agent agent = new Agent( RANDOM_ID, HOSTNAME, PARENT_NAME, MAC_ADDRESS, IPS, true, null );
    private Set<Agent> agents = Sets.newHashSet( agent );


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

        when( agentManager.getAgents() ).thenReturn( agents );


        Response response = restService.getAgents();


        verify( agentManager ).getAgents();
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( agents, JsonUtil.fromJson( response.getEntity().toString(), new TypeToken<Set<Agent>>()
        {
        }.getType() ) );
    }


    @Test
    public void shouldReturnPhysicalAgents()
    {
        when( agentManager.getPhysicalAgents() ).thenReturn( agents );


        Response response = restService.getPhysicalAgents();


        verify( agentManager ).getPhysicalAgents();
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( agents, JsonUtil.fromJson( response.getEntity().toString(), new TypeToken<Set<Agent>>()
        {
        }.getType() ) );
    }


    @Test
    public void shouldReturnLxcAgents()
    {

        when( agentManager.getLxcAgents() ).thenReturn( agents );


        Response response = restService.getLxcAgents();


        verify( agentManager ).getLxcAgents();
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( agents, JsonUtil.fromJson( response.getEntity().toString(), new TypeToken<Set<Agent>>()
        {
        }.getType() ) );
    }


    @Test
    public void shouldReturnAgentByHostname()
    {

        when( agentManager.getAgentByHostname( HOSTNAME ) ).thenReturn( agent );


        Response response = restService.getAgentByHostname( HOSTNAME );


        verify( agentManager ).getAgentByHostname( HOSTNAME );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( agent, JsonUtil.fromJson( response.getEntity().toString(), Agent.class ) );
    }


    @Test
    public void shouldMissAgentByHostname()
    {

        Response response = restService.getAgentByHostname( HOSTNAME );


        verify( agentManager ).getAgentByHostname( HOSTNAME );
        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void shouldReturnAgentByUUID()
    {

        when( agentManager.getAgentByUUID( RANDOM_ID ) ).thenReturn( agent );


        Response response = restService.getAgentByUUID( RANDOM_ID.toString() );


        verify( agentManager ).getAgentByUUID( RANDOM_ID );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( agent, JsonUtil.fromJson( response.getEntity().toString(), Agent.class ) );
    }


    @Test
    public void shouldMissAgentByUUID()
    {

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


    @Test
    public void shouldReturnAgentsByParentHostname()
    {

        when( agentManager.getLxcAgentsByParentHostname( HOSTNAME ) ).thenReturn( agents );


        Response response = restService.getLxcAgentsByParentHostname( HOSTNAME );


        verify( agentManager ).getLxcAgentsByParentHostname( HOSTNAME );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( agents, JsonUtil.fromJson( response.getEntity().toString(), new TypeToken<Set<Agent>>()
        {
        }.getType() ) );
    }


//    @Test
//    public void shouldReturnAgentsByEnvId()
//    {
//
//        when( agentManager.getAgentsByEnvironmentId( RANDOM_ID ) ).thenReturn( agents );
//
//
//        Response response = restService.getAgentsByEnvironmentId( RANDOM_ID.toString() );
//
//
//        verify( agentManager ).getAgentsByEnvironmentId( RANDOM_ID );
//        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
//        assertEquals( agents, JsonUtil.fromJson( response.getEntity().toString(), new TypeToken<Set<Agent>>()
//        {
//        }.getType() ) );
//    }
//
//
//    @Test
//    public void shouldFailByInvalidEnvId()
//    {
//
//        Response response = restService.getAgentsByEnvironmentId( null );
//
//
//        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
//    }
}
