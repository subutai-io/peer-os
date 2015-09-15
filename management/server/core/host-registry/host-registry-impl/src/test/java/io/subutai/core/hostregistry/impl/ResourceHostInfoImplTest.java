package io.subutai.core.hostregistry.impl;


import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ResourceHostInfo;

import com.google.common.collect.Maps;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;


public class ResourceHostInfoImplTest
{
    private static final String HOST_HOSTNAME = "host";
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String HOST_ID2 = UUID.randomUUID().toString();
    private static final String HOST_IP = "127.0.0.2";
    private static final String HOST_MAC_ADDRESS = "0c:8b:fd:c0:ea:fe";
    private static final String CONTAINER_HOSTNAME = "container";
    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_IP = "127.0.0.1";
    private static final String CONTAINER_INTERFACE = "eth0";
    private static final String CONTAINER_MAC_ADDRESS = "0c:8b:fd:c0:ea:fe";
    private static final ContainerHostState CONTAINER_STATUS = ContainerHostState.FROZEN;
    private static final HostArchitecture ARCH = HostArchitecture.AMD64;

    private static final String INFO_JSON =
            String.format( "{\"type\":\"HEARTBEAT\", \"hostname\":\"%s\", \"id\":\"%s\", \"arch\":\"%s\", " +
                            "\"interfaces\" : [{ \"interfaceName\":\"%s\", \"ip\":\"%s\",\"mac\":\"%s\"}], "
                            + "\"containers\": [{ \"hostname\":\"%s\", \"id\":\"%s\", " +
                            "\"interfaces\" : [{ \"interfaceName\":\"%s\", \"ip\":\"%s\",\"mac\":\"%s\"}], " +
                            "\"status\":\"%s\" , \"arch\":\"%s\"}]}", HOST_HOSTNAME, HOST_ID, ARCH,
                    Common.DEFAULT_CONTAINER_INTERFACE, HOST_IP, HOST_MAC_ADDRESS, CONTAINER_HOSTNAME, CONTAINER_ID,
                    CONTAINER_INTERFACE, CONTAINER_IP, CONTAINER_MAC_ADDRESS, CONTAINER_STATUS, ARCH );
    private static final String INFO2_JSON =
            String.format( "{\"type\":\"HEARTBEAT\", \"hostname\":\"%s\", \"id\":\"%s\", \"arch\":\"%s\", " +
                            "\"interfaces\" : [{ \"interfaceName\":\"%s\", \"ip\":\"%s\",\"mac\":\"%s\"}], "
                            + "\"containers\": [{ \"hostname\":\"%s\", \"id\":\"%s\", " +
                            "\"interfaces\" : [{ \"interfaceName\":\"%s\", \"ip\":\"%s\",\"mac\":\"%s\"}], " +
                            "\"status\":\"%s\" , \"arch\":\"%s\"}]}", HOST_HOSTNAME, HOST_ID2, ARCH,
                    Common.DEFAULT_CONTAINER_INTERFACE, HOST_IP, HOST_MAC_ADDRESS, CONTAINER_HOSTNAME, CONTAINER_ID,
                    CONTAINER_INTERFACE, CONTAINER_IP, CONTAINER_MAC_ADDRESS, CONTAINER_STATUS, ARCH );

    ResourceHostInfoImpl resourceHostInfo;


    @Before
    public void setUp() throws Exception
    {
        resourceHostInfo = JsonUtil.fromJson( INFO_JSON, ResourceHostInfoImpl.class );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( HOST_HOSTNAME, resourceHostInfo.getHostname() );
        assertEquals( HOST_ID, resourceHostInfo.getId() );
        assertEquals( HOST_IP, resourceHostInfo.getInterfaces().iterator().next().getIp() );
        assertEquals( ARCH, resourceHostInfo.getArch() );
    }


    @Test
    public void testGetContainers() throws Exception
    {
        Set<ContainerHostInfo> info = resourceHostInfo.getContainers();

        assertFalse( info.isEmpty() );

        ContainerHostInfo containerHostInfo = info.iterator().next();

        assertEquals( CONTAINER_HOSTNAME, containerHostInfo.getHostname() );
        assertEquals( CONTAINER_ID, containerHostInfo.getId() );
        assertEquals( CONTAINER_STATUS, containerHostInfo.getStatus() );
    }


    @Test
    public void testToString() throws Exception
    {
        String toString = resourceHostInfo.toString();

        assertThat( toString, containsString( HOST_HOSTNAME ) );
        assertThat( toString, containsString( HOST_IP ) );
        assertThat( toString, containsString( HOST_ID.toString() ) );
        assertThat( toString, containsString( HOST_MAC_ADDRESS ) );
        assertThat( toString, containsString( CONTAINER_HOSTNAME ) );
        assertThat( toString, containsString( CONTAINER_IP ) );
        assertThat( toString, containsString( CONTAINER_ID.toString() ) );
        assertThat( toString, containsString( CONTAINER_STATUS.name() ) );
        assertThat( toString, containsString( ARCH.name() ) );
    }


    @Test
    public void testEqualsHashCode() throws Exception
    {

        //check equals
        ResourceHostInfo resourceHostInfo2 = JsonUtil.fromJson( INFO_JSON, ResourceHostInfoImpl.class );

        assertEquals( resourceHostInfo, resourceHostInfo2 );

        assertFalse( resourceHostInfo.equals( new Object() ) );

        ResourceHostInfo resourceHostInfo3 = JsonUtil.fromJson( INFO2_JSON, ResourceHostInfoImpl.class );

        assertNotEquals( resourceHostInfo, resourceHostInfo3 );

        //check hash
        Map<ResourceHostInfo, ResourceHostInfo> map = Maps.newHashMap();

        map.put( resourceHostInfo2, resourceHostInfo2 );

        assertEquals( resourceHostInfo2, map.get( resourceHostInfo ) );
    }


    @Test
    public void testCompare() throws Exception
    {
        assertEquals( -1, resourceHostInfo.compareTo( null ) );
        ResourceHostInfo resourceHostInfo2 = JsonUtil.fromJson( INFO_JSON, ResourceHostInfoImpl.class );

        assertEquals( 0, resourceHostInfo.compareTo( resourceHostInfo2 ) );
    }
}
