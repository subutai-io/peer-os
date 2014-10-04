package org.safehaus.subutai.core.network.impl;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;

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
}
