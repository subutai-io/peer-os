package org.safehaus.subutai.plugin.common.mock;


import com.google.common.collect.Sets;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class CommonMockBuilder {

    public static Map<Agent, Set<Agent>> getLxcMap() {
        Agent agent = CommonMockBuilder.createAgent();
        Map<Agent, Set<Agent>> lxcMap = new HashMap<>();

        lxcMap.put( agent, Sets.newHashSet( agent ) );

        return lxcMap;
    }


    public static Agent createAgent() {
        return new Agent( UUID.randomUUID(), "127.0.0.1", "", "00:00:00:00", Arrays.asList( "127.0.0.1", "127.0.0.1" ),
                true, "transportId", UUID.randomUUID(), UUID.randomUUID() );
    }
}
