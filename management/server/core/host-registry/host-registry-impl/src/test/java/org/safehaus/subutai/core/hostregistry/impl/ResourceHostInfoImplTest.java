package org.safehaus.subutai.core.hostregistry.impl;


import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostState;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;


public class ResourceHostInfoImplTest
{
    private static final String HOST_HOSTNAME = "host";
    private static final UUID HOST_ID = UUID.randomUUID();
    private static final String HOST_IP = "127.0.0.2";
    private static final String HOST_INTERFACE = "eth0";
    private static final String HOST_MAC_ADDRESS = "0c:8b:fd:c0:ea:fe";
    private static final String CONTAINER_HOSTNAME = "container";
    private static final UUID CONTAINER_ID = UUID.randomUUID();
    private static final String CONTAINER_IP = "127.0.0.1";
    private static final String CONTAINER_INTERFACE = "eth0";
    private static final String CONTAINER_MAC_ADDRESS = "0c:8b:fd:c0:ea:fe";
    private static final ContainerHostState CONTAINER_STATUS = ContainerHostState.FROZEN;
    private static final String INFO_JSON =
            String.format( "{\"type\":\"HEARTBEAT\", \"hostname\":\"%s\", \"id\":\"%s\", " +
                            "\"interfaces\" : [{ \"interfaceName\":\"%s\", \"ip\":\"%s\",\"mac\":\"%s\"}], "
                            + "\"containers\": [{ \"hostname\":\"%s\", \"id\":\"%s\", " +
                            "\"interfaces\" : [{ \"interfaceName\":\"%s\", \"ip\":\"%s\",\"mac\":\"%s\"}], " +
                            "\"status\":\"%s\" }]}", HOST_HOSTNAME, HOST_ID, HOST_INTERFACE, HOST_IP, HOST_MAC_ADDRESS,
                    CONTAINER_HOSTNAME, CONTAINER_ID, CONTAINER_INTERFACE, CONTAINER_IP, CONTAINER_MAC_ADDRESS,
                    CONTAINER_STATUS );

    ResourceHostInfoImpl hostInfo;


    @Before
    public void setUp() throws Exception
    {
        hostInfo = JsonUtil.fromJson( INFO_JSON, ResourceHostInfoImpl.class );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( HOST_HOSTNAME, hostInfo.getHostname() );
        assertEquals( HOST_ID, hostInfo.getId() );
        assertEquals( HOST_IP, hostInfo.getInterfaces().iterator().next().getIp());
    }


    @Test
    public void testGetContainers() throws Exception
    {
        Set<ContainerHostInfo> info = hostInfo.getContainers();

        assertFalse( info.isEmpty() );

        ContainerHostInfo containerHostInfo = info.iterator().next();

        assertEquals( CONTAINER_HOSTNAME, containerHostInfo.getHostname() );
        assertEquals( CONTAINER_ID, containerHostInfo.getId() );
        assertEquals( CONTAINER_STATUS, containerHostInfo.getStatus() );
    }


    @Test
    public void testToString() throws Exception
    {
        String toString = hostInfo.toString();

        assertThat( toString, containsString( HOST_HOSTNAME ) );
        assertThat( toString, containsString( HOST_IP ) );
        assertThat( toString, containsString( HOST_ID.toString() ) );
        assertThat( toString, containsString( HOST_MAC_ADDRESS ) );
        assertThat( toString, containsString( CONTAINER_HOSTNAME ) );
        assertThat( toString, containsString( CONTAINER_IP ) );
        assertThat( toString, containsString( CONTAINER_ID.toString() ) );
        assertThat( toString, containsString( CONTAINER_STATUS.name() ) );
    }
}
