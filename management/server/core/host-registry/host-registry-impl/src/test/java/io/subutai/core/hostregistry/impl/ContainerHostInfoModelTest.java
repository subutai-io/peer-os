package io.subutai.core.hostregistry.impl;


import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.host.ContainerHostInfo;

import com.google.common.collect.Maps;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;


public class ContainerHostInfoModelTest
{

    private static final String HOSTNAME = "container1";
    private static final String ID = UUID.randomUUID().toString();
    private static final String ID2 = UUID.randomUUID().toString();
    private static final ContainerHostState CONTAINER_STATUS = ContainerHostState.FROZEN;
    private static final HostArchitecture ARCH = HostArchitecture.AMD64;
    private static final String IP = "127.0.0.1";
    private static final String MAC = "0c:8b:fd:c0:ea:fe";
    private static final String INFO_JSON = String.format(
            "{ \"hostname\":\"%s\", \"id\":\"%s\", \"interfaces\" : [{ \"interfaceName\":\"%s\", "
                    + "\"ip\":\"%s\",\"mac\":\"%s\"}],  \"status\":\"%s\", \"arch\":\"%s\" }", HOSTNAME, ID,
            Common.DEFAULT_CONTAINER_INTERFACE, IP, MAC, CONTAINER_STATUS, ARCH );
    private static final String INFO2_JSON = String.format(
            "{ \"hostname\":\"%s\", \"id\":\"%s\", \"interfaces\" : [{ \"interfaceName\":\"%s\", "
                    + "\"ip\":\"%s\",\"mac\":\"%s\"}],  \"status\":\"%s\", \"arch\":\"%s\" }", HOSTNAME, ID2,
            Common.DEFAULT_CONTAINER_INTERFACE, IP, MAC, CONTAINER_STATUS, ARCH );


    ContainerHostInfoModel containerHostInfo;


    @Before
    public void setUp() throws Exception
    {
        containerHostInfo = JsonUtil.fromJson( INFO_JSON, ContainerHostInfoModel.class );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( HOSTNAME, containerHostInfo.getHostname() );
        assertEquals( ID, containerHostInfo.getId() );
        assertEquals( CONTAINER_STATUS, containerHostInfo.getStatus() );
        assertEquals( ARCH, containerHostInfo.getArch() );
        assertEquals( Common.DEFAULT_CONTAINER_INTERFACE,
                containerHostInfo.getInterfaces().iterator().next().getName() );
        assertEquals( MAC, containerHostInfo.getInterfaces().iterator().next().getMac() );
        assertEquals( IP, containerHostInfo.getInterfaces().iterator().next().getIp() );
    }


    @Test
    public void testToString() throws Exception
    {
        String toString = containerHostInfo.toString();

        assertThat( toString, containsString( HOSTNAME ) );
        assertThat( toString, containsString( IP ) );
        assertThat( toString, containsString( ID.toString() ) );
        assertThat( toString, containsString( CONTAINER_STATUS.name() ) );
        assertThat( toString, containsString( ARCH.name() ) );
    }


    @Test
    public void testEqualsHashCode() throws Exception
    {

        //check equals
        ContainerHostInfo containerHostInfo2 = JsonUtil.fromJson( INFO_JSON, ContainerHostInfoModel.class );

        assertEquals( containerHostInfo, containerHostInfo2 );

        assertFalse( containerHostInfo.equals( new Object() ) );

        ContainerHostInfo containerHostInfo3 = JsonUtil.fromJson( INFO2_JSON, ContainerHostInfoModel.class );

        assertNotEquals( containerHostInfo, containerHostInfo3 );

        //check hash
        Map<ContainerHostInfo, ContainerHostInfo> map = Maps.newHashMap();

        map.put( containerHostInfo2, containerHostInfo2 );

        assertEquals( containerHostInfo2, map.get( containerHostInfo ) );
    }


    @Test
    public void testCompare() throws Exception
    {
        assertEquals( -1, containerHostInfo.compareTo( null ) );
        ContainerHostInfo containerHostInfo2 = JsonUtil.fromJson( INFO_JSON, ContainerHostInfoModel.class );

        assertEquals( 0, containerHostInfo.compareTo( containerHostInfo2 ) );
    }
}

