package org.safehaus.kiskis.mgmt.product.common.test.unit.mock;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import com.google.common.collect.Sets;


public class CommonMockBuilder {

    public static Agent createAgent() {
        return new Agent( UUID.randomUUID(), "127.0.0.1", "", "00:00:00:00", Arrays.asList( "127.0.0.1", "127.0.0.1" ),
                true, "transportId" );
    }

    public static Map<Agent, Set<Agent>> getLxcMap() {
        Agent agent = CommonMockBuilder.createAgent();
        Map<Agent, Set<Agent>> lxcMap = new HashMap<>();

        lxcMap.put( agent, Sets.newHashSet( agent ) );

        return lxcMap;
    }

}
