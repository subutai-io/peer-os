package org.safehaus.subutai.plugin.common.mock;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.UUIDUtil;

import com.google.common.collect.Sets;


public class CommonMockBuilder
{

    public static Map<Agent, Set<Agent>> getLxcMap()
    {
        Agent agent = CommonMockBuilder.createAgent();
        Map<Agent, Set<Agent>> lxcMap = new HashMap<>();

        lxcMap.put( agent, Sets.newHashSet( agent ) );

        return lxcMap;
    }


    public static Agent createAgent()
    {
        return new Agent( UUIDUtil.generateTimeBasedUUID(), "127.0.0.1", "", "00:00:00:00",
                Arrays.asList( "127.0.0.1", "127.0.0.1" ), true, "transportId" );
    }
}
