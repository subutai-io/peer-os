package org.safehaus.kiskis.mgmt.impl.solr.util;

import java.util.Arrays;
import java.util.UUID;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class TestUtil
{
    public static Agent getAgent()
    {
        return new Agent( UUID.randomUUID(), "hostname", "parenthost", "MAC-addr", Arrays.asList( "127.0.0.1" ), true,
                "transportId" );
    }
}
