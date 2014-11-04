package org.safehaus.subutai.core.security.impl;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;

import com.google.common.collect.Lists;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by dilshat on 9/30/14.
 */
public class MockUtils
{

    public static Agent getAgent( UUID agentUUID, String hostname, String ip )
    {
        Agent agent = mock( Agent.class );
        when( agent.getUuid() ).thenReturn( agentUUID );
        when( agent.getHostname() ).thenReturn( hostname );
        when( agent.getListIP() ).thenReturn( Lists.newArrayList( ip ) );

        return agent;
    }


    public static Container getContainer( UUID agentUUID, String hostname, String ip )
    {
        Container container = mock( Container.class );
        when( container.getAgentId() ).thenReturn( agentUUID );
        when( container.getHostname() ).thenReturn( hostname );
        when( container.getIps() ).thenReturn( Lists.newArrayList( ip ) );

        return container;
    }
}
