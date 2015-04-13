package org.safehaus.subutai.core.env.rest;


import java.util.UUID;

import org.safehaus.subutai.common.host.ContainerHostState;


public class TestUtil
{
    public static final String TEMPLATE_NAME = "master";
    public static final int NUMBER_OF_CONTAINERS = 3;
    public static final String SUBNET = "192.168.1.0/24";
    public static final boolean ASYNC = true;
    public static final boolean FORCE = true;
    public static final UUID ENV_ID = UUID.randomUUID();
    public static final UUID CONTAINER_ID = UUID.randomUUID();
    public static final String HOSTNAME = "hostname";
    public static final String NODE_GROUP_NAME = "node group";
    public static final boolean IS_CONNECTED = true;
    public static final String IP = "127.0.0.1";
    public static final String ENV_NAME = "environment";
    public static final String SSH_KEY = "KEY";
    public static final ContainerHostState CONTAINER_STATE = ContainerHostState.RUNNING;
    public static final UUID PEER_ID = UUID.randomUUID();

    public static final String TOPOLOGY_JSON = String.format(
            "{ \"nodeGroupPlacement\": {\"%s\": [" + "{\"name\": \"Sample Node Group\"," + "\"templateName\": \"%s\","
                    + "\"numberOfContainers\": 4,\"sshGroupId\": 1,"
                    + "\"hostsGroupId\": 1,\"containerPlacementStrategy\": {"
                    + "\"strategyId\": \"ROUND_ROBIN\",\"criteria\": []" + "}}]}}", PEER_ID, TEMPLATE_NAME );
}
