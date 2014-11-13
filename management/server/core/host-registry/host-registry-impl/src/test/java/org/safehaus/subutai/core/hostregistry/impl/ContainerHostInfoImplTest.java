package org.safehaus.subutai.core.hostregistry.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostState;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class ContainerHostInfoImplTest
{

    private static final String HOSTNAME = "container1";
    private static final UUID ID = UUID.randomUUID();
    private static final ContainerHostState CONTAINER_STATUS = ContainerHostState.FROZEN;
    private static final String INTERFACE = "eth0";
    private static final String IP = "127.0.0.1";
    private static final String MAC = "0c:8b:fd:c0:ea:fe";
    private static final String INFO_JSON = String.format(
            "{ \"hostname\":\"%s\", \"id\":\"%s\", \"interfaces\" : [{ \"interfaceName\":\"%s\", "
                    + "\"ip\":\"%s\",\"mac\":\"%s\"}],  \"status\":\"%s\" }", HOSTNAME, ID, INTERFACE, IP, MAC,
            CONTAINER_STATUS );


    ContainerHostInfoImpl containerHostInfo;


    @Before
    public void setUp() throws Exception
    {
        containerHostInfo = JsonUtil.fromJson( INFO_JSON, ContainerHostInfoImpl.class );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( HOSTNAME, containerHostInfo.getHostname() );
        assertEquals( ID, containerHostInfo.getId() );
        assertEquals( CONTAINER_STATUS, containerHostInfo.getStatus() );
        assertEquals( INTERFACE, containerHostInfo.getInterfaces().iterator().next().getInterfaceName() );
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
    }


}

