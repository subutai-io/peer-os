package org.safehaus.subutai.core.agent.rest;


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
        Agent agent =
                new Agent( UUID.randomUUID(), "hostname", "parentName", "MAC", Lists.newArrayList( "127.0.0.1" ), true,
                        null, UUID.randomUUID(), UUID.randomUUID() );
        when( agentManager.getAgents() ).thenReturn( Sets.newHashSet( agent ) );
        Response response = restService.getAgents();

        verify( agentManager ).getAgents();
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertEquals( Sets.newHashSet( agent ),
                JsonUtil.fromJson( response.getEntity().toString(), new TypeToken<Set<Agent>>()
                {}.getType() ) );
    }
}
