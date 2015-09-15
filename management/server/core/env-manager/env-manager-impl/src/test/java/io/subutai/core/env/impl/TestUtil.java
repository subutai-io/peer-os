package io.subutai.core.env.impl;


import java.util.Set;
import java.util.UUID;

import io.subutai.common.settings.Common;

import com.google.common.collect.Sets;


public class TestUtil
{
    public static final String TEMPLATE_NAME = "master";
    public static final int NUMBER_OF_CONTAINERS = 3;
    public static final String SUBNET = "192.168.1.0/24";
    public static final String GATEWAY_IP = "192.168.1.1";
    public static final String ENV_ID = UUID.randomUUID().toString();
    public static final String CONTAINER_ID = UUID.randomUUID().toString();
    public static final String INTERFACE_NAME = "eth0";
    public static final String MAC = "mac";
    public static final String NODE_GROUP_NAME = "node group";
    public static final String IP = "127.0.0.1";
    public static final String PEER_ID = UUID.randomUUID().toString();
    public static final String LOCAL_PEER_ID = UUID.randomUUID().toString();
    public static final String DEFAULT_DOMAIN = "intra.lan";
    public static final Long VNI = Common.MIN_VNI_ID;
    public static final int VLAN = Common.MIN_VLAN_ID;
    public static final Long INTERFACE_ID = Long.MAX_VALUE;
    public static final int HOSTS_GROUP_ID = 1;
    public static final int SSH_GROUP_ID = 1;
    public static final String HOSTNAME = "hostname";
    public static final String TAG = "tag";
    public static final int PID = 123;
    public static final int RAM_MB = 2048;
    public static final int CPU_QUOTA = 30;
    public static final Set<Integer> CPU_SET = Sets.newHashSet( 1, 3, 5 );
    public static final String ENV_NAME = "environment";
    public static final String SSH_KEY = "ssh key";
    public static final Long USER_ID = 123l;
    public static final int LAST_USED_IP_IDX = 123;
    public static final String ERR_MSG = "error";
    public static final String PEER_NAME = "peer";
    public static final UUID BLUEPRINT_ID = UUID.randomUUID();
}
