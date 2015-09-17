package io.subutai.core.environment.rest;


import java.util.UUID;

import io.subutai.common.host.ContainerHostState;


public class TestUtil
{
    public static final String TEMPLATE_NAME = "master";
    public static final String SUBNET = "192.168.1.0/24";
    public static final String ENV_ID = UUID.randomUUID().toString();
    public static final String CONTAINER_ID = UUID.randomUUID().toString();
    public static final String HOSTNAME = "hostname";
    public static final String IP = "127.0.0.1";
    public static final String ENV_NAME = "environment";
    public static final String SSH_KEY = "KEY";
    public static final ContainerHostState CONTAINER_STATE = ContainerHostState.RUNNING;
    public static final String PEER_ID = UUID.randomUUID().toString();

    public static final String TOPOLOGY_JSON = String.format(
            "{ \"nodeGroupPlacement\": {\"%s\": [" + "{\"name\": \"Sample Node Group\"," + "\"templateName\": \"%s\","
                    + "\"numberOfContainers\": 4,\"sshGroupId\": 1,"
                    + "\"hostsGroupId\": 1,\"containerPlacementStrategy\": {"
                    + "\"strategyId\": \"ROUND_ROBIN\",\"criteria\": []" + "}}]}}", PEER_ID, TEMPLATE_NAME );
}
