package org.safehaus.kiskis.mgmt.impl.hive.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import org.junit.*;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class AbstractHandlerTest {

    private final AbstractHandlerImpl impl;
    private final String serverHostname = "server-host";
    private Config config;

    public AbstractHandlerTest() {
        impl = new AbstractHandlerImpl();
    }

    @Before
    public void setUp() {
        config = new Config();
        config.setClusterName("hive-cluster");

        Agent a = new Agent(UUID.randomUUID(), serverHostname, "parent-host",
                "MAC-addr", Arrays.asList("127.0.0.1"), true, "transportId");
        config.setServer(a);
        config.setClients(new HashSet<Agent>(4));
        for(int i = 1; i < 3; i++) {
            a = new Agent(UUID.randomUUID(), "host" + i, "parent-host",
                    "MAC-addr" + i, Arrays.asList("127.0.0." + (i + 1)), true,
                    "transportId" + i);
            config.getClients().add(a);
        }
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIsServerNode() {
        Assert.assertTrue(impl.isServerNode(config, serverHostname));
        Assert.assertFalse(impl.isServerNode(config, "other-name"));
    }

    public class AbstractHandlerImpl extends AbstractHandler {

        public AbstractHandlerImpl() {
            super(null, "");
        }

        public void run() {
        }

        @Override
        public UUID getTrackerId() {
            return UUID.randomUUID();
        }
    }

}
