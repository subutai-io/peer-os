package org.safehaus.subutai.core.containerregistry.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.containerregistry.api.ContainerHostState;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class ContainerHostInfoImplTest
{

    private static final String HOSTNAME = "container1";
    private static final UUID ID = UUID.randomUUID();
    private static final ContainerHostState status = ContainerHostState.FROZEN;
    private static final String IP = "127.0.0.1";
    private static final String INFO_JSON =
            String.format( "{ \"hostname\":\"%s\", \"id\":\"%s\", \"ips\" : [\"%s\"], \"status\":\"%s\" }", HOSTNAME,
                    ID, IP, status );


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
        assertEquals( IP, containerHostInfo.getIps().iterator().next() );
        assertEquals( status, containerHostInfo.getStatus() );
    }


    @Test
    public void testToString() throws Exception
    {
        String toString = containerHostInfo.toString();

        assertThat( toString, containsString( HOSTNAME ) );
        assertThat( toString, containsString( IP ) );
        assertThat( toString, containsString( ID.toString() ) );
        assertThat( toString, containsString( status.name() ) );
    }
}

