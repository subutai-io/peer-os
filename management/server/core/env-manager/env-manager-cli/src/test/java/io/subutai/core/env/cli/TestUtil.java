package io.subutai.core.env.cli;


import java.util.UUID;


public class TestUtil
{
    public static final String TEMPLATE_NAME = "master";
    public static final int NUMBER_OF_CONTAINERS = 3;
    public static final String SUBNET = "192.168.1.0/24";
    public static final boolean ASYNC = true;
    public static final boolean FORCE = true;
    public static final String ENV_ID = UUID.randomUUID().toString();
    public static final String CONTAINER_ID = UUID.randomUUID().toString();
    public static final String HOSTNAME = "hostname";
    public static final String NODE_GROUP_NAME = "node group";
    public static final boolean IS_CONNECTED = true;
    public static final String IP = "127.0.0.1";
    public static final String ENV_NAME = "environment";
}
