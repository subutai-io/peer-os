/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.agent.impl;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.UUIDUtil;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test mocking utilities
 */
public class MockUtils
{

    public static Response getRegistrationRequestFromLxcAgent()
    {
        Response response = mock( Response.class );

        when( response.getUuid() ).thenReturn( UUIDUtil.generateTimeBasedUUID() );
        when( response.isLxc() ).thenReturn( true );
        when( response.getIps() ).thenReturn( mock( List.class ) );
        when( response.getHostname() ).thenReturn( "lxchostname" );
        when( response.getParentHostName() ).thenReturn( "hostname" );
        when( response.getType() ).thenReturn( ResponseType.REGISTRATION_REQUEST );

        return response;
    }


    public static Response getRegistrationRequestFromPhysicalAgent()
    {
        Response response = mock( Response.class );

        when( response.getUuid() ).thenReturn( UUIDUtil.generateTimeBasedUUID() );
        when( response.isLxc() ).thenReturn( false );
        when( response.getHostname() ).thenReturn( "hostname" );
        when( response.getIps() ).thenReturn( mock( List.class ) );
        when( response.getType() ).thenReturn( ResponseType.REGISTRATION_REQUEST );

        return response;
    }


    public static Response getRegistrationRequestFromLxcAgentWithEnvironmentId( UUID envId )
    {
        Response response = getRegistrationRequestFromLxcAgent();
        when( response.getEnvironmentId() ).thenReturn( envId );
        return response;
    }


    public static Set<Agent> getAgents( UUID... agentUUIDs )
    {
        Set<Agent> agents = new HashSet<>();
        for ( UUID agentUUID : agentUUIDs )
        {
            agents.add( getAgent( agentUUID ) );
        }

        return agents;
    }


    public static Agent getAgent( UUID agentUUID )
    {
        Agent agent = mock( Agent.class );
        when( agent.getUuid() ).thenReturn( agentUUID );

        return agent;
    }
}
