package io.subutai.core.environment.rest;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import io.subutai.common.host.ContainerHostState;

import static junit.framework.TestCase.assertEquals;


public class ContainerDtoTest
{
    private static final String HOSTNAME = "host123";
    private static final ContainerHostState STATE = ContainerHostState.FROZEN;
    private static final String IP = "192.168.1.1";
    private static final String TEMPLATE_NAME = "hadoop";
    ContainerDto containerDto;


    @Before
    public void setUp() throws Exception
    {
        containerDto =
                new ContainerDto( TestUtil.CONTAINER_ID, TestUtil.ENV_ID, TestUtil.HOSTNAME, TestUtil.CONTAINER_STATE,
                        TestUtil.IP, TestUtil.TEMPLATE_NAME );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( TestUtil.ENV_ID, containerDto.getEnvironmentId() );
        assertEquals( TestUtil.CONTAINER_ID, containerDto.getId() );
        assertEquals( TestUtil.HOSTNAME, containerDto.getHostname() );
        assertEquals( TestUtil.IP, containerDto.getIp() );
        assertEquals( TestUtil.TEMPLATE_NAME, containerDto.getTemplateName() );
        assertEquals( TestUtil.CONTAINER_STATE, containerDto.getState() );
    }


    @Test
    public void testSetters() throws Exception
    {
        String uuid = UUID.randomUUID().toString();

        containerDto.setEnvironmentId( uuid );

        assertEquals( uuid, containerDto.getEnvironmentId() );

        containerDto.setId( uuid );

        assertEquals( uuid, containerDto.getId() );


        containerDto.setHostname( HOSTNAME );

        assertEquals( HOSTNAME, containerDto.getHostname() );

        containerDto.setState( STATE );

        assertEquals( STATE, containerDto.getState() );

        containerDto.setIp( IP );

        assertEquals( IP, containerDto.getIp() );

        containerDto.setTemplateName( TEMPLATE_NAME );

        assertEquals( TEMPLATE_NAME, containerDto.getTemplateName() );
    }
}
