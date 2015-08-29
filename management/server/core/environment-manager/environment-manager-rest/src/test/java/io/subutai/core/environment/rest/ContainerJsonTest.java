package io.subutai.core.environment.rest;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import io.subutai.common.host.ContainerHostState;

import static junit.framework.TestCase.assertEquals;


public class ContainerJsonTest
{
    private static final String HOSTNAME = "host123";
    private static final ContainerHostState STATE = ContainerHostState.FROZEN;
    private static final String IP = "192.168.1.1";
    private static final String TEMPLATE_NAME = "hadoop";
    ContainerJson containerJson;


    @Before
    public void setUp() throws Exception
    {
        containerJson =
                new ContainerJson( TestUtil.CONTAINER_ID, TestUtil.ENV_ID, TestUtil.HOSTNAME, TestUtil.CONTAINER_STATE,
                        TestUtil.IP, TestUtil.TEMPLATE_NAME );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( TestUtil.ENV_ID, containerJson.getEnvironmentId() );
        assertEquals( TestUtil.CONTAINER_ID, containerJson.getId() );
        assertEquals( TestUtil.HOSTNAME, containerJson.getHostname() );
        assertEquals( TestUtil.IP, containerJson.getIp() );
        assertEquals( TestUtil.TEMPLATE_NAME, containerJson.getTemplateName() );
        assertEquals( TestUtil.CONTAINER_STATE, containerJson.getState() );
    }


    @Test
    public void testSetters() throws Exception
    {
        UUID uuid = UUID.randomUUID();

        containerJson.setEnvironmentId( uuid );

        assertEquals( uuid, containerJson.getEnvironmentId() );

        containerJson.setId( uuid );

        assertEquals( uuid, containerJson.getId() );


        containerJson.setHostname( HOSTNAME );

        assertEquals( HOSTNAME, containerJson.getHostname() );

        containerJson.setState( STATE );

        assertEquals( STATE, containerJson.getState() );

        containerJson.setIp( IP );

        assertEquals( IP, containerJson.getIp() );

        containerJson.setTemplateName( TEMPLATE_NAME );

        assertEquals( TEMPLATE_NAME, containerJson.getTemplateName() );
    }
}
